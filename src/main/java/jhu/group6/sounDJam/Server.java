package jhu.group6.sounDJam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.javalin.Javalin;
import io.javalin.JavalinEvent;
import io.javalin.websocket.WsSession;
import jhu.group6.sounDJam.controllers.*;
import jhu.group6.sounDJam.repositories.MongoRepository;
import jhu.group6.sounDJam.utils.SongRecommendationLimiter;
import jhu.group6.sounDJam.utils.ValidUserHandler;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Server {
    public static boolean dev = false;

    private static MongoRepository repository;
    private static ObjectMapper json = new ObjectMapper();

    public static Map<String, ArrayList<WsSession>> wsSessions = new HashMap<>();

    private static String DATABASE_NAME = dev ?
            "local" :
            "heroku_m86d3xzb";
    private static ConnectionString CONNECTION = dev ?
            new ConnectionString("mongodb://127.0.0.1:27017") :
            new ConnectionString("mongodb://heroku_m86d3xzb:utiadfdu7vtnplmf1rtovvc443@ds157571.mlab.com:57571/heroku_m86d3xzb");

    public static void main(String[] args) throws UnknownHostException {
        Javalin app = Javalin.create();

        //This is so heroku knows what port to put itself on.
        int port = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 7000 ;

        app.event(JavalinEvent.SERVER_STARTING, () -> {
            try {
                MongoClient mongoClient = MongoClients.create(CONNECTION);
                repository = new MongoRepository(mongoClient, DATABASE_NAME);
            } catch (Exception e) {
                //TODO: LOG THIS ERROR PROPERLY
                System.out.println(e.getMessage());
                System.out.print("MongoDB is not working");
            }
        });

        app.ws("/v1/session/:session-id/queue/play", ws -> {
            ws.onConnect((connection) -> {
                var sessionId = connection.pathParam("session-id");
                var connectedClients = wsSessions.get(sessionId);
                if (connectedClients == null) connectedClients = new ArrayList<>();
                connectedClients.add(connection);

                wsSessions.put(sessionId, connectedClients);
                System.out.println("Connected");
            });

            ws.onMessage((wsSession, message) -> {
                var sessionId = wsSession.pathParam("session-id");
                var connectedClients = wsSessions.get(sessionId);
                var newSong = json.writeValueAsString(SpotifyController.playNext(wsSession, message));
                connectedClients.stream().filter(WsSession::isOpen).forEach(session -> session.send(newSong));
            });

            ws.onClose((session, statusCode, reason) -> {
                var sessionId = session.pathParam("session-id");
                var connectedClients = wsSessions.get(sessionId);

                connectedClients.remove(session);
                if (connectedClients.size() == 0) {
                    wsSessions.remove(sessionId);
                } else {
                    wsSessions.put(sessionId, connectedClients);
                }

                System.out.println("Closed");
            });

            ws.onError((session, throwable) -> System.out.println("Error"));
        });
      
        app.start(port);
        app.routes(() -> {
            path("/v1/health-check", () -> {
                get(HealthCheckController::healthCheck);
            });

            path("/v1/spotify", () -> {
                get(SpotifyController::createApiInstance);
                path("login_success", () -> {
                    get(SpotifyController::onLoginSuccess);
                });
            });

            path("/v1/:session-id/get-graph", () -> {
                get(QueueController::getGraph);
            });

            path("/v1/session", () -> {
                get(SessionController::createNewSession);
                path(":session-id", () -> {
                    before(ValidUserHandler::ensureValidUserForSession);
                    before(ValidUserHandler::ensureValidDJForSession);
                    before(SongRecommendationLimiter::ensureUserCanRecommend);
                    before(SessionController::updateLastUpdated);

                    path("user", () -> {
                        post(SessionController::addUser);
                        path(":user-id", () -> {
                            delete(SessionController::removeUser);
                        });
                    });

                    path("timer", () -> {
                        post(SpotifyController::updateTimer);
                    });

                    path("end", () -> {
                        delete(SessionController::deleteSession);
                    });

                    path("song", () -> {
                        get(SpotifyController::getCurrentlyPlayingSong);
                    });

                    path("boo", () -> {
                        post(SessionController::booCurrentlyPlayingSong);
                    });

                    path("queue", () -> {
                        get(QueueController::getQueue);
                        path("pop-next", () -> {
                            put(SpotifyController::popNext);
                        });
                    });

                    path("search", () -> {
                        get(SpotifyController::searchSong);
                    });

                    path("search-artist", () -> {
                        get(SpotifyController::searchArtist);
                    });

                    path("recommend", () -> {
                        post(QueueController::recommendSong);
                    });

                    path("settings", () -> {
                        get(SettingController::getSetting);
                        post(SettingController::postSetting);
                    });

//                    path("token", () -> {
//                        get(SpotifyController::getAccessToken);
//                        path("swap", () -> {
//                            post(SpotifyController::swapAccessToken);
//                        });
//                        path("refresh", () -> {
//                            post(SpotifyController::refreshAccessToken);
//                        });
//                    });
                });
            });
        });
        app.after(ctx -> {
            ctx.sessionAttribute("user", null);
            ctx.sessionAttribute("session", null);
            ctx.sessionAttribute("queue", null);
            ctx.sessionAttribute("setting", null);
        });
        app.error(404, ctx -> {
            ctx.result("The page you are trying to access does not exist");
        });
    }

    public static MongoRepository getMongoRepository() { return repository; }
  
    public static ObjectMapper getJson() {
        return json;
    }
}
