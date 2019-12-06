package tests.controllers;

import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import io.javalin.BadRequestResponse;
import io.javalin.Context;
import jhu.group6.sounDJam.Server;
import jhu.group6.sounDJam.controllers.SessionController;
import jhu.group6.sounDJam.controllers.SpotifyController;
import jhu.group6.sounDJam.models.Session;
import jhu.group6.sounDJam.models.Song;
import jhu.group6.sounDJam.repositories.MongoRepository;
import jhu.group6.sounDJam.utils.CollectionNames;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Server.class, Session.class, SpotifyController.class})
public class SessionControllerTest {

    @Test
    public void createNewSession() {
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        PowerMockito.mockStatic(Server.class);

        var contextMock = mock(Context.class);
        var mongoMock = mock(MongoRepository.class);

        when(contextMock.queryParam("accessToken")).thenReturn("accessToken");
        when(contextMock.queryParam("refreshToken")).thenReturn("refreshToken");
        when(contextMock.queryParam("redirectUri")).thenReturn("redirectUri");
        when(Server.getMongoRepository()).thenReturn(mongoMock);
        PowerMockito.doNothing().when(mongoMock).insertIntoCollection(any(), any());
        PowerMockito.doNothing().when(mongoMock).insertIntoCollection(any(), any());
        PowerMockito.doNothing().when(mongoMock).insertIntoCollection(any(), any());
        PowerMockito.doNothing().when(mongoMock).insertIntoCollection(any(), any());

        SessionController.createNewSession(contextMock);

        verify(contextMock).redirect(any(), valueCaptureStatus.capture());
        assertEquals(valueCaptureStatus.getValue(), 302, 0);
    }

    @Test
    public void createNewSessionNull() {
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        PowerMockito.mockStatic(Server.class);

        var contextMock = mock(Context.class);
        var mongoMock = mock(MongoRepository.class);

        when(contextMock.queryParam("accessToken")).thenReturn(null);
        when(contextMock.queryParam("refreshToken")).thenReturn(null);
        when(contextMock.queryParam("redirectUri")).thenReturn(null);
        when(Server.getMongoRepository()).thenReturn(mongoMock);
        PowerMockito.doNothing().when(mongoMock).insertIntoCollection(any(), any());
        PowerMockito.doNothing().when(mongoMock).insertIntoCollection(any(), any());
        PowerMockito.doNothing().when(mongoMock).insertIntoCollection(any(), any());
        PowerMockito.doNothing().when(mongoMock).insertIntoCollection(any(), any());

        SessionController.createNewSession(contextMock);

        verify(contextMock).status(valueCaptureStatus.capture());
        assertEquals(valueCaptureStatus.getValue(), 201, 0);
    }

    @Test
    public void deleteSession() {
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(Session.class);

        var contextMock = mock(Context.class);
        var sessionMock = mock(Session.class);
        var mongoMock = mock(MongoRepository.class);

        var sessionId = UUID.randomUUID();

        // get session from context
        when(contextMock.sessionAttribute("session")).thenReturn(null).thenReturn(sessionMock);
        when(contextMock.pathParam(any())).thenReturn("sessionId");
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(new Document());
        PowerMockito.when(Session.fromDocument(any())).thenReturn(sessionMock);
        doNothing().when(contextMock).sessionAttribute("session", sessionMock);

        when(sessionMock.getSessionId()).thenReturn(sessionId);
        when(mongoMock.purgeAllBySessionId(sessionId.toString())).thenReturn((long) 5);
        when(sessionMock.getPartierIds()).thenReturn(new ArrayList<>() {{ add(UUID.randomUUID()); }});

        SessionController.deleteSession(contextMock);

        verify(contextMock).status(valueCaptureStatus.capture());
        assertEquals(valueCaptureStatus.getValue(), 202, 0);
    }

    @Test(expected = BadRequestResponse.class)
    public void deleteSessionMismatchCount() {
        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(Session.class);

        var contextMock = mock(Context.class);
        var sessionMock = mock(Session.class);
        var mongoMock = mock(MongoRepository.class);

        var sessionId = UUID.randomUUID();

        // get session from context
        when(contextMock.sessionAttribute("session")).thenReturn(null).thenReturn(sessionMock);
        when(contextMock.pathParam(any())).thenReturn("sessionId");
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(new Document());
        PowerMockito.when(Session.fromDocument(any())).thenReturn(sessionMock);
        doNothing().when(contextMock).sessionAttribute("session", sessionMock);

        when(sessionMock.getSessionId()).thenReturn(sessionId);
        when(mongoMock.purgeAllBySessionId(sessionId.toString())).thenReturn((long) 5);
        when(sessionMock.getPartierIds()).thenReturn(new ArrayList<>() {{}});

        SessionController.deleteSession(contextMock);
    }

    @Test(expected = BadRequestResponse.class)
    public void addUserCannotAddToMongo() {
        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(Session.class);

        var contextMock = mock(Context.class);
        var sessionMock = mock(Session.class);
        var mongoMock = mock(MongoRepository.class);

        var sessionId = UUID.randomUUID();
        var sessionDoc = new Document("session", "doc");

        // get session from context
        when(contextMock.sessionAttribute("session")).thenReturn(null).thenReturn(sessionMock);
        when(contextMock.pathParam(any())).thenReturn("sessionId");
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(new Document());
        PowerMockito.when(Session.fromDocument(any())).thenReturn(sessionMock);
        doNothing().when(contextMock).sessionAttribute("session", sessionMock);

        when(sessionMock.getPartierIds()).thenReturn(new ArrayList<>());
        when(sessionMock.getSessionId()).thenReturn(sessionId);
        when(sessionMock.toDocument()).thenReturn(sessionDoc);
        when(mongoMock.updateOneFromCollectionBySessionId(CollectionNames.SESSION, sessionId.toString(), sessionDoc)).thenReturn(false);

        SessionController.addUser(contextMock);
    }

    @Test
    public void addUser() {
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);
        var valueCaptureData = ArgumentCaptor.forClass(HashMap.class);

        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(Session.class);

        var contextMock = mock(Context.class);
        var sessionMock = mock(Session.class);
        var mongoMock = mock(MongoRepository.class);

        var sessionId = UUID.randomUUID();
        var sessionDoc = new Document("session", "doc");

        // get session from context
        when(contextMock.sessionAttribute("session")).thenReturn(null).thenReturn(sessionMock);
        when(contextMock.pathParam(any())).thenReturn("sessionId");
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(new Document());
        PowerMockito.when(Session.fromDocument(any())).thenReturn(sessionMock);
        doNothing().when(contextMock).sessionAttribute("session", sessionMock);

        when(sessionMock.getPartierIds()).thenReturn(new ArrayList<>());
        when(sessionMock.getSessionId()).thenReturn(sessionId);
        when(sessionMock.toDocument()).thenReturn(sessionDoc);
        when(mongoMock.updateOneFromCollectionBySessionId(CollectionNames.SESSION, sessionId.toString(), sessionDoc)).thenReturn(true);
        doNothing().when(mongoMock).insertIntoCollection(any(), any());

        SessionController.addUser(contextMock);

        verify(contextMock).json(valueCaptureData.capture());
        verify(contextMock).status(valueCaptureStatus.capture());
        assertEquals(valueCaptureData.getValue().get("sessionId"), sessionId.toString());
        assertEquals(valueCaptureStatus.getValue(), 201, 0);

        try {
            UUID.fromString(valueCaptureData.getValue().get("userId").toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test(expected = BadRequestResponse.class)
    public void removeUserCannotRemoveUserFromSession() {
        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(Session.class);

        var contextMock = mock(Context.class);
        var sessionMock = mock(Session.class);
        var mongoMock = mock(MongoRepository.class);

        var sessionId = UUID.randomUUID();
        var sessionDoc = new Document("session", "doc");
        var userId = UUID.randomUUID().toString();
        var partiers = new ArrayList<UUID>() {{
            add(UUID.fromString(userId));
        }};

        // get session from context
        when(contextMock.sessionAttribute("session")).thenReturn(null).thenReturn(sessionMock);
        when(contextMock.pathParam(any())).thenReturn("sessionId");
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(new Document());
        PowerMockito.when(Session.fromDocument(any())).thenReturn(sessionMock);
        doNothing().when(contextMock).sessionAttribute("session", sessionMock);

        when(contextMock.pathParam(":user-id")).thenReturn(userId);
        when(sessionMock.getPartierIds()).thenReturn(partiers);
        when(sessionMock.getSessionId()).thenReturn(sessionId);
        when(sessionMock.toDocument()).thenReturn(sessionDoc);
        when(mongoMock.updateOneFromCollectionBySessionId(CollectionNames.SESSION, sessionId.toString(), sessionDoc)).thenReturn(false);

        SessionController.removeUser(contextMock);
    }

    @Test(expected = BadRequestResponse.class)
    public void removeUserCannotRemoveUserFromDatabase() {
        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(Session.class);

        var contextMock = mock(Context.class);
        var sessionMock = mock(Session.class);
        var mongoMock = mock(MongoRepository.class);

        var sessionId = UUID.randomUUID();
        var sessionDoc = new Document("session", "doc");
        var userId = UUID.randomUUID().toString();
        var partiers = new ArrayList<UUID>() {{
            add(UUID.fromString(userId));
        }};

        // get session from context
        when(contextMock.sessionAttribute("session")).thenReturn(null).thenReturn(sessionMock);
        when(contextMock.pathParam(any())).thenReturn("sessionId");
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(new Document());
        PowerMockito.when(Session.fromDocument(any())).thenReturn(sessionMock);
        doNothing().when(contextMock).sessionAttribute("session", sessionMock);

        when(contextMock.pathParam(":user-id")).thenReturn(userId);
        when(sessionMock.getPartierIds()).thenReturn(partiers);
        when(sessionMock.getSessionId()).thenReturn(sessionId);
        when(sessionMock.toDocument()).thenReturn(sessionDoc);
        when(mongoMock.updateOneFromCollectionBySessionId(CollectionNames.SESSION, sessionId.toString(), sessionDoc)).thenReturn(true);
        when(mongoMock.removeOneFromCollectionById(CollectionNames.USER, userId)).thenReturn((long) 0);

        SessionController.removeUser(contextMock);
    }

    @Test
    public void removeUser() {
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(Session.class);

        var contextMock = mock(Context.class);
        var sessionMock = mock(Session.class);
        var mongoMock = mock(MongoRepository.class);

        var sessionId = UUID.randomUUID();
        var sessionDoc = new Document("session", "doc");
        var userId = UUID.randomUUID().toString();
        var partiers = new ArrayList<UUID>() {{
            add(UUID.fromString(userId));
        }};

        // get session from context
        when(contextMock.sessionAttribute("session")).thenReturn(null).thenReturn(sessionMock);
        when(contextMock.pathParam(any())).thenReturn("sessionId");
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(new Document());
        PowerMockito.when(Session.fromDocument(any())).thenReturn(sessionMock);
        doNothing().when(contextMock).sessionAttribute("session", sessionMock);

        when(contextMock.pathParam(":user-id")).thenReturn(userId);
        when(sessionMock.getPartierIds()).thenReturn(partiers);
        when(sessionMock.getSessionId()).thenReturn(sessionId);
        when(sessionMock.toDocument()).thenReturn(sessionDoc);
        when(mongoMock.updateOneFromCollectionBySessionId(CollectionNames.SESSION, sessionId.toString(), sessionDoc)).thenReturn(true);
        when(mongoMock.removeOneFromCollectionById(CollectionNames.USER, userId)).thenReturn((long) 1);

        SessionController.removeUser(contextMock);

        verify(contextMock).status(valueCaptureStatus.capture());
        assertEquals(valueCaptureStatus.getValue(), 202, 0);

    }

    @Test
    public void booCurrentlyPlayingSongSkipSong() throws InterruptedException, SpotifyWebApiException, IOException {
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(Session.class);
        PowerMockito.mockStatic(SpotifyController.class);

        var contextMock = mock(Context.class);
        var sessionMock = mock(Session.class);
        var songMock = mock(Song.class);
        var mongoMock = mock(MongoRepository.class);

        var sessionId = UUID.randomUUID();
        var sessionDoc = new Document("session", "doc");

        // get session from context
        when(contextMock.sessionAttribute("session")).thenReturn(null).thenReturn(sessionMock);
        when(contextMock.pathParam(any())).thenReturn("sessionId");
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(new Document());
        PowerMockito.when(Session.fromDocument(any())).thenReturn(sessionMock);
        doNothing().when(contextMock).sessionAttribute("session", sessionMock);

        when(sessionMock.getCurrentSong()).thenReturn(songMock);
        doNothing().when(songMock).boo();
        when(songMock.getBoos()).thenReturn(10);
        when(sessionMock.getPartierIds()).thenReturn(new ArrayList<>() {{
            add(UUID.randomUUID());
        }});
        PowerMockito.when(SpotifyController.getRedirectUri(contextMock)).thenReturn("redirectUri");
        when(sessionMock.getSessionId()).thenReturn(sessionId);
        when(sessionMock.toDocument()).thenReturn(sessionDoc);
        when(mongoMock.updateOneFromCollectionBySessionId(CollectionNames.SESSION, sessionId, sessionDoc)).thenReturn(true);

        SessionController.booCurrentlyPlayingSong(contextMock);

        verify(contextMock).status(valueCaptureStatus.capture());
        assertEquals(valueCaptureStatus.getValue(), 201, 0);
    }

    @Test
    public void booCurrentlyPlayingSongDontSkip() throws InterruptedException, SpotifyWebApiException, IOException {
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(Session.class);
        PowerMockito.mockStatic(SpotifyController.class);

        var contextMock = mock(Context.class);
        var sessionMock = mock(Session.class);
        var songMock = mock(Song.class);
        var mongoMock = mock(MongoRepository.class);

        // get session from context
        when(contextMock.sessionAttribute("session")).thenReturn(null).thenReturn(sessionMock);
        when(contextMock.pathParam(any())).thenReturn("sessionId");
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(new Document());
        PowerMockito.when(Session.fromDocument(any())).thenReturn(sessionMock);
        doNothing().when(contextMock).sessionAttribute("session", sessionMock);

        when(sessionMock.getCurrentSong()).thenReturn(songMock);
        doNothing().when(songMock).boo();
        when(songMock.getBoos()).thenReturn(0);
        when(sessionMock.getPartierIds()).thenReturn(new ArrayList<>() {{
            add(UUID.randomUUID());
        }});

        SessionController.booCurrentlyPlayingSong(contextMock);

        verify(contextMock).status(valueCaptureStatus.capture());
        assertEquals(valueCaptureStatus.getValue(), 201, 0);
    }

    @Test
    public void updateLastUpdated() {
        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(Session.class);

        var contextMock = mock(Context.class);
        var sessionMock = mock(Session.class);
        var mongoMock = mock(MongoRepository.class);

        var sessionId = UUID.randomUUID();
        var sessionDoc = new Document("session", "doc");

        // get session from context
        when(contextMock.sessionAttribute("session")).thenReturn(null).thenReturn(sessionMock);
        when(contextMock.pathParam(any())).thenReturn("sessionId");
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(new Document());
        PowerMockito.when(Session.fromDocument(any())).thenReturn(sessionMock);
        doNothing().when(contextMock).sessionAttribute("session", sessionMock);
        when(sessionMock.getSessionId()).thenReturn(sessionId);
        when(sessionMock.toDocument()).thenReturn(sessionDoc);

        when(mongoMock.updateOneFromCollectionBySessionId( CollectionNames.SESSION,
                sessionId,
                sessionDoc))
                .thenReturn(true);
    }
}