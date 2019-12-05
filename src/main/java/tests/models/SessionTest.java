package tests.models;

import jhu.group6.sounDJam.models.Session;
import jhu.group6.sounDJam.models.Song;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Song.class})
public class SessionTest {
    private String name = "sessionName";
    private UUID djId = UUID.randomUUID();
    private UUID sessionId = UUID.randomUUID();
    private UUID queueId = UUID.randomUUID();
    private UUID settingId = UUID.randomUUID();
    private String accessToken = "access token";
    private String refreshToken = "refresh token";
    private long lastUpdated = 1000L;
    private UUID state = UUID.randomUUID();

    private static List<UUID> partierIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());

    @Test
    public void toDocument() {
        var songDocument = new Document();
        var songMock = mock(Song.class);
        when(songMock.toDocument()).thenReturn(songDocument);

        var sessionDocFull = Session.builder()
                .sessionId(sessionId)
                .name(name)
                .djId(djId)
                .partierIds(partierIds)
                .currentSong(songMock)
                .queueId(queueId)
                .settingId(settingId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .state(state)
                .lastUpdated(lastUpdated)
                .build()
                .toDocument();

        var partierIdDocs = (List<String>) sessionDocFull.get("partierIds");
        var partierIdsFromDoc = partierIdDocs.stream().map(UUID::fromString).collect(toList());

        var currentSongDoc = (Document) sessionDocFull.get("currentSong");

        assertEquals(sessionDocFull.get("name"), name);
        assertEquals(sessionDocFull.get("djId"), djId.toString());
        assertEquals(sessionDocFull.get("settingId"), settingId.toString());
        assertEquals(sessionDocFull.get("queueId"), queueId.toString());
        assertEquals(sessionDocFull.get("accessToken"), accessToken);
        assertEquals(sessionDocFull.get("refreshToken"), refreshToken);
        assertEquals(partierIdsFromDoc, partierIds);
        assertEquals(sessionDocFull.get("sessionId"), sessionId.toString());
        assertEquals(sessionDocFull.get("state"), state.toString());
        assertEquals(sessionDocFull.get("lastUpdated"), lastUpdated);
        assertEquals(currentSongDoc, songDocument);

        var sessionDocEmpty = Session.builder()
                .sessionId(sessionId)
                .state(state)
                .build()
                .toDocument();

        assertNull(sessionDocEmpty.get("name"));
        assertNull(sessionDocEmpty.get("djId"));
        assertNull(sessionDocEmpty.get("settingId"));
        assertNull(sessionDocEmpty.get("queueId"));
        assertNull(sessionDocEmpty.get("accessToken"));
        assertNull(sessionDocEmpty.get("refreshToken"));
        assertNull(sessionDocEmpty.get("currentSong"));
        assertEquals(sessionDocEmpty.get("partierIds"), new ArrayList<String>());
        assertEquals(sessionDocEmpty.get("sessionId"), sessionId.toString());
        assertEquals(sessionDocEmpty.get("state"), state.toString());
        assertEquals(sessionDocEmpty.get("lastUpdated"), 0L);
    }

    @Test
    public void fromDocument() {
        var songDocument = new Document();
        var songMock = mock(Song.class);
        when(songMock.toDocument()).thenReturn(songDocument);

        mockStatic(Song.class);
        when(Song.fromDocument(songDocument)).thenReturn(songMock);

        var sessionDocFull = Session.builder()
                .sessionId(sessionId)
                .state(state)
                .name(name)
                .djId(djId)
                .partierIds(partierIds)
                .currentSong(songMock)
                .queueId(queueId)
                .settingId(settingId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .lastUpdated(lastUpdated)
                .build()
                .toDocument();

        var sessionFull = Session.fromDocument(sessionDocFull);

        assertEquals(sessionFull.getName(), name);
        assertEquals(sessionFull.getDjId(), djId);
        assertEquals(sessionFull.getSettingId(), settingId);
        assertEquals(sessionFull.getQueueId(), queueId);
        assertEquals(sessionFull.getAccessToken(), accessToken);
        assertEquals(sessionFull.getRefreshToken(), refreshToken);
        assertEquals(sessionFull.getPartierIds(), partierIds);
        assertEquals(sessionFull.getSessionId(), sessionId);
        assertEquals(sessionFull.getState(), state);
        assertEquals(sessionFull.getLastUpdated(), lastUpdated);
        assertEquals(sessionFull.getCurrentSong(), songMock);


        when(songMock.toDocument()).thenReturn(songDocument);
        when(Song.fromDocument(songDocument)).thenReturn(songMock);

        var sessionDocEmpty = Session.builder()
                .build()
                .toDocument();

        var sessionEmpty = Session.fromDocument(sessionDocEmpty);

        assertNull(sessionEmpty.getName());
        assertNull(sessionEmpty.getDjId());
        assertNull(sessionEmpty.getSettingId());
        assertNull(sessionEmpty.getQueueId());
        assertNull(sessionEmpty.getAccessToken());
        assertNull(sessionEmpty.getRefreshToken());
        assertNull(sessionEmpty.getCurrentSong());
        assertEquals(sessionEmpty.getPartierIds(), new ArrayList<>());
        assertEquals(sessionEmpty.getLastUpdated(), 0);
    }

    @Test
    public void testGetSetSessionId() {
        var session = Session.builder().sessionId(sessionId).build();
        assertEquals(sessionId, session.getSessionId());
        session.setSessionId(state);
        assertEquals(state, session.getSessionId());
    }

    @Test
    public void testGetSetName() {
        var session = Session.builder().build();
        assertNull(session.getName());
        session.setName(name);
        assertEquals(name, session.getName());
    }

    @Test
    public void testGetSetDjId() {
        var session = Session.builder().build();
        assertNull(session.getName());
        session.setName(name);
        assertEquals(name, session.getName());
    }

    @Test
    public void testGetSetPartierIds() {
        var session = Session.builder().build();
        assertNull(session.getDjId());
        session.setDjId(djId);
        assertEquals(djId, session.getDjId());
    }

    @Test
    public void testGetSetCurrentSong() {
        var song = Song.builder().build();

        var session = Session.builder().build();
        assertNull(session.getCurrentSong());
        session.setCurrentSong(song);
        assertEquals(song, session.getCurrentSong());
    }

    @Test
    public void testGetSetSettingId() {
        var session = Session.builder().build();
        assertNull(session.getSettingId());
        session.setSettingId(settingId);
        assertEquals(settingId, session.getSettingId());
    }

    @Test
    public void testGetSetQueueId() {
        var session = Session.builder().build();
        assertNull(session.getQueueId());
        session.setQueueId(queueId);
        assertEquals(queueId, session.getQueueId());
    }

    @Test
    public void testGetSetAccessToken() {
        var session = Session.builder().build();
        assertNull(session.getAccessToken());
        session.setAccessToken(accessToken);
        assertEquals(accessToken, session.getAccessToken());
    }

    @Test
    public void testGetSetRefreshToken() {
        var session = Session.builder().build();
        assertNull(session.getRefreshToken());
        session.setRefreshToken(refreshToken);
        assertEquals(refreshToken, session.getRefreshToken());
    }

    @Test
    public void testGetSetState() {
        var session = Session.builder().state(state).build();
        assertEquals(state, session.getState());
        session.setState(djId);
        assertEquals(djId, session.getState());
    }

    @Test
    public void testGetSetLastUpdated() {
        var session = Session.builder().build();
        assertEquals(0, session.getLastUpdated(), 0);
        session.setLastUpdated(System.currentTimeMillis());
        assertEquals(System.currentTimeMillis(), session.getLastUpdated(), 10);
    }
}