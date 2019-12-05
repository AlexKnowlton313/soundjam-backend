package jhu.group6.sounDJam.controllers;

import com.google.gson.JsonArray;
import com.google.gson.Gson;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import io.javalin.BadRequestResponse;
import io.javalin.Context;
import io.javalin.websocket.WsSession;
import jhu.group6.sounDJam.Server;
import jhu.group6.sounDJam.models.Queue;
import jhu.group6.sounDJam.models.Session;
import jhu.group6.sounDJam.models.Song;
import jhu.group6.sounDJam.utils.CollectionNames;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static jhu.group6.sounDJam.controllers.SessionController.getSessionFromContext;
import static jhu.group6.sounDJam.controllers.SessionController.getSessionFromId;
import static jhu.group6.sounDJam.controllers.SettingController.getSettingFromContext;
import static jhu.group6.sounDJam.controllers.SettingController.getSettingFromId;

public class SpotifyController {
    private static final String spotifyAuthURL = "https://accounts.spotify.com/api/token";
    private static final String spotifyClientId = "ae0185648d2849b8b89f06b05fe14880";
    private static final String spotifyClientSecret = "41c748d7cef245698a41a91f00162e04";
    public static Map<String, ScheduledFuture> timers = new HashMap<>();

    private static SpotifyApi buildSpotifyApi(String redirectUri) {
        var redirect = SpotifyHttpManager.makeUri(redirectUri);
        return new SpotifyApi.Builder()
                .setClientId(spotifyClientId)
                .setClientSecret(spotifyClientSecret)
                .setRedirectUri(redirect)
                .build();
    }

    static String getRedirectUri(Context ctx) {
        var host = ctx.host();
        return getRedirectUriFromHost(host);
    }

    private static String getRedirectUriFromWsSession(WsSession wsSession) {
        var host = wsSession.host();
        return getRedirectUriFromHost(host);
    }

    private static String getRedirectUriFromHost(String host) {
        if (host == null) {
            throw new BadRequestResponse("invalid host");
        }
        var redirectUri = host.equals("soundjam.herokuapp.com") ? "https://" : "http://";
        redirectUri += host + "/v1/spotify/";
        return redirectUri;
    }

    public static void createApiInstance(Context ctx) {
        var redirectUri = getRedirectUri(ctx);
        final SpotifyApi spotifyApi = buildSpotifyApi(redirectUri);
        var error = ctx.queryParam("error");

        if (error != null) {
            return;
        }

        var code = ctx.queryParam("code");
        if (spotifyApi != null && code != null) {
            final AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code)
                    .build();
            try {
                final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();
                var accessToken = authorizationCodeCredentials.getAccessToken();
                var refreshToken = authorizationCodeCredentials.getRefreshToken();
                var redirectTo = "/v1/session?accessToken=" + accessToken + "&refreshToken=" + refreshToken + "&redirectUri=" + redirectUri;
                ctx.redirect(redirectTo);
            } catch (IOException | SpotifyWebApiException e) {
                throw new BadRequestResponse("Invalid authorization code");
            }
        } else {
            throw new BadRequestResponse("Something went wrong :(");
        }
    }

    static AudioFeatures getAudioFeaturesFromSongId(Context ctx, String songId) {
        var session = getSessionFromContext(ctx);
        var redirectUri = getRedirectUri(ctx);
        var spotifyApi = buildSpotifyApi(redirectUri);;
        spotifyApi.setAccessToken(session.getAccessToken());
        spotifyApi.setRefreshToken(session.getRefreshToken());
        try {
            return spotifyApi.getAudioFeaturesForTrack(songId)
                    .build()
                    .execute();
        } catch (Exception e) {
            return null;
        }
    }

    public static void onLoginSuccess(Context ctx) {
        var ids = new String[] {ctx.queryParam("djId"), ctx.queryParam("sessionId")};
        ctx.json(ids);
        ctx.status(200);
    }

    public static void searchSong(Context ctx) {
        var songInfo = ctx.queryParam("song");
        if (songInfo == null) {
            throw new BadRequestResponse("No song information found in query parameter!");
        }

        var redirectUri = getRedirectUri(ctx);
        var spotifyApi = buildSpotifyApi(redirectUri);
        var session = getSessionFromContext(ctx);
        spotifyApi.setAccessToken(session.getAccessToken());
        spotifyApi.setRefreshToken(session.getRefreshToken());

        var searchTracksRequest = spotifyApi.searchTracks(songInfo)
                .limit(10)
                .offset(0)
                .build();
        try {
            final Paging<Track> trackPaging = searchTracksRequest.execute();
            Track[] tracks = trackPaging.getItems();
            ctx.json(tracks);
            ctx.status(200);
        } catch (IOException | SpotifyWebApiException e) {
            throw new BadRequestResponse("Something went wrong while searching");
        }
    }

    public static Song playNext(WsSession wsSession, String userId) throws IOException, SpotifyWebApiException, InterruptedException {
        var user = UserController.getUserFromId(userId);
        var session = SessionController.getSessionFromId(user.getSessionId().toString());
        var redirectUri = getRedirectUriFromWsSession(wsSession);
        return playNextSong(session, redirectUri);
    }

    static Song playNextSong(Session session, String redirectUri) throws IOException, SpotifyWebApiException, InterruptedException {
        var queue = QueueController.getQueueFromId(session.getSessionId().toString());
        var song = queue.popNextSong();
        var spotifyApi = SpotifyController.buildSpotifyApi(redirectUri);
        spotifyApi.setAccessToken(session.getAccessToken());
        spotifyApi.setRefreshToken(session.getRefreshToken());

        song = setNullSong(queue, song, spotifyApi);
        var nextSong = new JsonArray();
        nextSong.add("spotify:track:" + song.getSpotifySongId());

        spotifyApi.startResumeUsersPlayback()
                .uris(nextSong)
                .build()
                .execute();
        session.setCurrentSong(song);

        Server.getMongoRepository().updateOneFromCollectionBySessionId(
                CollectionNames.QUEUE,
                queue.getSessionId(),
                queue.toDocument());

        Server.getMongoRepository().updateOneFromCollectionBySessionId(
                CollectionNames.SESSION,
                session.getSessionId(),
                session.toDocument());
        TimeUnit.SECONDS.sleep(1);
        updateTimerForSession(session, redirectUri);
        return song;
    }

    private static Song setNullSong(Queue queue, Song song, SpotifyApi spotifyApi) throws IOException, SpotifyWebApiException {
        if (song == null) {
            var setting = getSettingFromId(queue.getSessionId().toString());

            var recommendations = spotifyApi.getRecommendations()
                    .seed_tracks(queue.getPlayedSongIdsAsString())
                    .target_danceability((float) setting.getDanceability())
                    .target_duration_ms((int) setting.getMaxSongLength()
                                        - (setting.getMaxSongLength() - setting.getMinSongLength()) / 2)
                    .target_energy((float) setting.getEnergy())
                    .target_tempo((float) setting.getTempo())
                    .target_valence((float) setting.getValence())
                    .build()
                    .execute()
                    .getTracks();
            var i = 0;
            String trackId = "";
            while (i < recommendations.length) {
                trackId = recommendations[i].getId();
                if (queue.getPlayedSongIds().contains(trackId)) {
                    break;
                }
                i++;
            }
            queue.addPlayedSongId(trackId);
            var track = spotifyApi.getTrack(trackId)
                    .build()
                    .execute();

            song = Song.builder()
                    .spotifySongId(track.getId())
                    .album(track.getAlbum().getName())
                    .albumArt(track.getAlbum().getImages()[0].getUrl())
                    .artist(track.getArtists()[0].getName())
                    .name(track.getName())
                    .build();
        }
        return song;
    }

    public static void updateTimer(Context context) throws IOException, SpotifyWebApiException {
        var session = SessionController.getSessionFromContext(context);
        var redirectUri = getRedirectUri(context);
        updateTimerForSession(session, redirectUri);
    }

    private static void updateTimerForSession(Session session, String redirectUri) throws IOException, SpotifyWebApiException {
        broadcastBackgroundSongChange(session);
        var sessionId = session.getSessionId().toString();
        var currTimer = timers.get(sessionId);
        if (currTimer != null) {
            currTimer.cancel(true);
            timers.remove(sessionId);
        }
        var spotifyApi = SpotifyController.buildSpotifyApi(redirectUri);
        spotifyApi.setAccessToken(session.getAccessToken());
        spotifyApi.setRefreshToken(session.getRefreshToken());
        CurrentlyPlaying currentlyPlaying = spotifyApi.getUsersCurrentlyPlayingTrack().build().execute();
        if (currentlyPlaying != null) {
            if (currentlyPlaying.getIs_playing()) {
                Track currentTrack = currentlyPlaying.getItem();
                if (currentTrack != null) {
                    if (currentTrack.getDurationMs() != 0 && currentlyPlaying.getProgress_ms() != 0) {
                        var timeLeft = currentTrack.getDurationMs() - currentlyPlaying.getProgress_ms() - 2000;
                        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
                        var newState = UUID.randomUUID();
                        session.setState(newState);
                        ScheduledFuture songTimer = service.schedule(() -> {
                            try {
                                var currSession = getSessionFromId(sessionId);
                                if (newState.equals(currSession.getState())) {
                                    playNextSong(session, redirectUri);
                                }
                            } catch (Exception e) {
                                System.err.println("Error updating song");
                            }
                        }, timeLeft, TimeUnit.MILLISECONDS);
                        timers.put(sessionId, songTimer);
                        Server.getMongoRepository().updateOneFromCollectionBySessionId(
                                CollectionNames.SESSION,
                                session.getSessionId(),
                                session.toDocument());
                    }
                }
            }
        }
    }

    private static void broadcastBackgroundSongChange(Session session) throws IOException {
        var sessionId = session.getSessionId().toString();
        var connectedClients = Server.wsSessions.get(sessionId);
        var currSong = Server.getJson().writeValueAsString(session.getCurrentSong());
        connectedClients.stream().filter(WsSession::isOpen).forEach(wsSession -> wsSession.send(currSong));
    }

    public static void popNext(Context context) throws IOException, SpotifyWebApiException {
        var session = SessionController.getSessionFromContext(context);
        var queue = QueueController.getQueueFromContext(context);
        var song = queue.popNextSong();
        if (song == null && queue.getPlayedSongIds().size() == 0) {
            context.result("none");
            context.status(201);
            return;
        }

        var spotifyApi = SpotifyController.buildSpotifyApi(getRedirectUri(context));
        spotifyApi.setAccessToken(session.getAccessToken());
        spotifyApi.setRefreshToken(session.getRefreshToken());

        song = setNullSong(queue, song, spotifyApi);
        var nextSong = new JsonArray();
        nextSong.add("spotify:track:" + song.getSpotifySongId());
        session.setCurrentSong(song);

        Server.getMongoRepository().updateOneFromCollectionBySessionId(
                CollectionNames.QUEUE,
                queue.getSessionId(),
                queue.toDocument());

        Server.getMongoRepository().updateOneFromCollectionBySessionId(
                CollectionNames.SESSION,
                session.getSessionId(),
                session.toDocument());
        var songMap = new HashMap<String, String>();
        songMap.put("songid", song.getSpotifySongId());
        songMap.put("albumurl", song.getAlbumArt());
        songMap.put("name", song.getName());
        songMap.put("artist", song.getArtist());
        context.json(songMap);
        context.status(201);
    }

    public static void getCurrentlyPlayingSong(Context context) throws IOException, SpotifyWebApiException {
        var session = getSessionFromContext(context);
        var queue = QueueController.getQueueFromId(session.getSessionId().toString());
        var spotifyApi = SpotifyController.buildSpotifyApi(getRedirectUri(context));
        spotifyApi.setAccessToken(session.getAccessToken());
        spotifyApi.setRefreshToken(session.getRefreshToken());
        CurrentlyPlaying currentlyPlaying = spotifyApi.getUsersCurrentlyPlayingTrack().build().execute();
        var song = new HashMap<String, String>();
        if (currentlyPlaying == null || currentlyPlaying.getItem() == null) {
            context.result("{}");
            context.status(200);
            return;
        }
        var track = currentlyPlaying.getItem();
        song.put("name", track.getName());
        song.put("artist", track.getArtists()[0].getName());
        song.put("albumArt", track.getAlbum().getImages()[0].getUrl());
        if (session.getCurrentSong() == null || !track.getId().equals(session.getCurrentSong().getSpotifySongId())) {
            var audioFeatures = getAudioFeaturesFromSongId(context, track.getId());
            var currentSong = Song.builder()
                    .spotifySongId(track.getId())
                    .album(track.getAlbum().getName())
                    .albumArt(song.get("albumArt"))
                    .artist(song.get("artist"))
                    .name(song.get("name"))
                    .build();
            currentSong.setAudioFeatures(audioFeatures);
            session.setCurrentSong(currentSong);
            Server.getMongoRepository().updateOneFromCollectionBySessionId(
                    CollectionNames.SESSION,
                    session.getSessionId(),
                    session.toDocument());
        }
        if (queue.getPlayedSongIds().size() == 0) {
            queue.addPlayedSongId(track.getId());
            Server.getMongoRepository().updateOneFromCollectionBySessionId(
                    CollectionNames.QUEUE,
                    queue.getSessionId(),
                    queue.toDocument());
        }
        context.json(song);
        context.status(200);
    }

    public static void getAccessToken(Context context) {
        var session = getSessionFromContext(context);
        context.result(session.getAccessToken());
        context.status(200);
    }

    static AuthorizationCodeCredentials swapAccessTokenForSession(String sessionId, String redirectUri) {
        try {
            var session = getSessionFromId(sessionId);
            var spotifyApi = buildSpotifyApi(redirectUri);
            spotifyApi.setAccessToken(session.getAccessToken());
            spotifyApi.setRefreshToken(session.getRefreshToken());
            AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();
            session.setAccessToken(authorizationCodeCredentials.getAccessToken());
            Server.getMongoRepository().updateOneFromCollectionBySessionId(
                    CollectionNames.SESSION,
                    session.getSessionId(),
                    session.toDocument());
            return authorizationCodeCredentials;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static void swapAccessToken(Context context) {
        var session = getSessionFromContext(context);
        var authorizationCodeCredentials = swapAccessTokenForSession(session.getSessionId().toString(), getRedirectUri(context));
        var credentialsMap = new HashMap<String, Object>();
        if (authorizationCodeCredentials != null) {
            credentialsMap.put("access_token", authorizationCodeCredentials.getAccessToken());
            credentialsMap.put("refresh_token", session.getRefreshToken());
            credentialsMap.put("expires_in", authorizationCodeCredentials.getExpiresIn());
        }
        context.json(credentialsMap);
        context.status(202);
    }

    public static void refreshAccessToken(Context context) {
        var session = getSessionFromContext(context);
        var authorizationCodeCredentials = swapAccessTokenForSession(session.getSessionId().toString(), getRedirectUri(context));
        var credentialsMap = new HashMap<String, Object>();
        if (authorizationCodeCredentials != null) {
            credentialsMap.put("access_token", authorizationCodeCredentials.getAccessToken());
            credentialsMap.put("expires_in", authorizationCodeCredentials.getExpiresIn());
        }
        context.json(credentialsMap);
        context.status(202);
    }

    public static void searchArtist(Context ctx) {
        var artistInfo = ctx.queryParam("artist");
        if (artistInfo == null) {
            throw new BadRequestResponse("No artist information found in query parameter!");
        }

        var redirectUri = getRedirectUri(ctx);
        var spotifyApi = buildSpotifyApi(redirectUri);
        var session = getSessionFromContext(ctx);
        spotifyApi.setAccessToken(session.getAccessToken());
        spotifyApi.setRefreshToken(session.getRefreshToken());

        var searchArtistsRequest = spotifyApi.searchArtists(artistInfo)
                .limit(10)
                .offset(0)
                .build();
        try {
            final Paging<Artist> artistPaging = searchArtistsRequest.execute();
            Artist[] artists = artistPaging.getItems();
            ctx.json(artists);
            ctx.status(200);
        } catch (IOException | SpotifyWebApiException e) {
            throw new BadRequestResponse("Something went wrong while searching");
        }
    }
}
