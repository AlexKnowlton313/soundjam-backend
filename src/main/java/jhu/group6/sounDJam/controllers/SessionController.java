package jhu.group6.sounDJam.controllers;

import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import io.javalin.BadRequestResponse;
import io.javalin.Context;
import jhu.group6.sounDJam.Server;
import jhu.group6.sounDJam.exceptions.InvalidSessionIdException;
import jhu.group6.sounDJam.models.*;
import jhu.group6.sounDJam.utils.CollectionNames;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class SessionController {
    public static void createNewSession(Context context) {
        var accessToken = context.queryParam("accessToken");
        var refreshToken = context.queryParam("refreshToken");
        var redirectUri = context.queryParam("redirectUri");

        var dj = User.builder().build();
        var setting = Setting.builder().build();
        var queue = Queue.builder().build();
        var session = Session.builder()
                .settingId(setting.getSettingId())
                .queueId(queue.getQueueId())
                .name("New Session")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .djId(dj.getUserId())
                .build();

        dj.setSessionId(session.getSessionId());
        setting.setSessionId(session.getSessionId());
        queue.setSessionId(session.getSessionId());

        var db = Server.getMongoRepository();
        db.insertIntoCollection(CollectionNames.USER, dj.toDocument());
        db.insertIntoCollection(CollectionNames.SESSION, session.toDocument());
        db.insertIntoCollection(CollectionNames.SETTING, setting.toDocument());
        db.insertIntoCollection(CollectionNames.QUEUE, queue.toDocument());

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> { // counldnt figure out how to test :(
            var currSession = getSessionFromId(session.getSessionId().toString());
            if (System.currentTimeMillis() - currSession.getLastUpdated() > 1800000) {
                Server.getMongoRepository().purgeAllBySessionId(session.getSessionId().toString());
                SpotifyController.timers.remove(session.getSessionId().toString());
                scheduler.shutdown();
            }
            try {
                SpotifyController.swapAccessTokenForSession(session.getSessionId().toString(), redirectUri);
            } catch (Exception e) {
                throw new BadRequestResponse("Something went wrong :(");
            }
        }, 29, 29, TimeUnit.MINUTES);

        if (accessToken != null && refreshToken != null) {
            var djId = session.getDjId().toString();
            var sessionId = session.getSessionId().toString();
            var redirectTo ="/v1/spotify/login_success?djId=" + djId + "&sessionId=" + sessionId;
            context.redirect(redirectTo, 302);
        } else {
            context.json(session);
            context.status(201);
        }
    }

    public static void deleteSession(Context context) {
        var session = getSessionFromContext(context);
        var deleteCount = Server.getMongoRepository().purgeAllBySessionId(session.getSessionId().toString());
        var expectedCount = 4 + session.getPartierIds().size();
        SpotifyController.timers.remove(session.getSessionId().toString());

        if (deleteCount != expectedCount)
            throw new BadRequestResponse("Unable to delete all Session objects");
        context.status(202);
    }

    public static void addUser(Context context) {
        var session = getSessionFromContext(context);
        var userId = UUID.randomUUID();

        session.getPartierIds().add(userId);

        if (!Server.getMongoRepository().updateOneFromCollectionBySessionId(
                CollectionNames.SESSION,
                session.getSessionId().toString(),
                session.toDocument()))
             throw new BadRequestResponse("Failed to add user " + userId.toString());

        var user = User.builder()
                 .nickname(userId.toString())
                 .sessionId(session.getSessionId())
                 .userId(userId)
                 .build()
                 .toDocument();

        Server.getMongoRepository().insertIntoCollection(CollectionNames.USER, user);
        var idMap = new HashMap<String, String>();
        idMap.put("userId", userId.toString());
        idMap.put("sessionId", session.getSessionId().toString());
        context.json(idMap);
        context.status(201);
    }

    public static void removeUser(Context context) {
        var userToDeleteId = UUID.fromString(context.pathParam(":user-id"));
        var session = getSessionFromContext(context);

        session.getPartierIds().remove(userToDeleteId);
        if (!Server.getMongoRepository().updateOneFromCollectionBySessionId(
                CollectionNames.SESSION,
                session.getSessionId().toString(),
                session.toDocument()))
            throw new BadRequestResponse("Failed to remove user " + userToDeleteId.toString());

        if (1 != Server.getMongoRepository().removeOneFromCollectionById(
                CollectionNames.USER,
                userToDeleteId.toString()))
            throw new BadRequestResponse("Failed to delete user" + userToDeleteId.toString() + " from database.");

        context.status(202);
    }

    public static void booCurrentlyPlayingSong(Context context) throws IOException, SpotifyWebApiException, InterruptedException {
        var session = getSessionFromContext(context);
        session.getCurrentSong().boo();

        if (!skipIfMajorityBoo(context)) {
            Server.getMongoRepository().updateOneFromCollectionBySessionId(
                    CollectionNames.SESSION,
                    session.getSessionId(),
                    session.toDocument());
        }

        context.status(201);
    }

    private static boolean skipIfMajorityBoo(Context context) throws IOException, SpotifyWebApiException, InterruptedException {
        var session = getSessionFromContext(context);
        var currentBoos = session.getCurrentSong().getBoos();
        var booThreshold = session.getPartierIds().size() / 2;

        if (currentBoos > booThreshold) {
            SpotifyController.playNextSong(session, SpotifyController.getRedirectUri(context));
            return true;
        }

        return false;
    }

    public static Session getSessionFromContext(Context context) {
        if (context.sessionAttribute("session") == null) {
            var sessionId = context.pathParam("session-id");
            Session session = getSessionFromId(sessionId);
            context.sessionAttribute("session", session);
        }
        return context.sessionAttribute("session");
    }

    public static Session getSessionFromId(String sessionId) {
        var sessionDoc = Server.getMongoRepository().findOneFromCollectionBySessionId(CollectionNames.SESSION, sessionId);
        if (sessionDoc == null) throw new InvalidSessionIdException(sessionId);
        return Session.fromDocument(sessionDoc);
    }

    public static void updateLastUpdated(Context context) {
        var session = getSessionFromContext(context);
        session.setLastUpdated(System.currentTimeMillis());
        Server.getMongoRepository().updateOneFromCollectionBySessionId(
                CollectionNames.SESSION,
                session.getSessionId().toString(),
                session.toDocument());
    }
}
