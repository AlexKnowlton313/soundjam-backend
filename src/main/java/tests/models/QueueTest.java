package tests.models;

import jhu.group6.sounDJam.controllers.UserController;
import jhu.group6.sounDJam.models.Queue;
import jhu.group6.sounDJam.models.Session;
import jhu.group6.sounDJam.models.Song;
import jhu.group6.sounDJam.models.User;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.naming.directory.InvalidAttributesException;
import java.time.Instant;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UserController.class, Song.class})
public class QueueTest {
    private UUID queueId = UUID.randomUUID();
    private UUID sessionId = UUID.randomUUID();
    private double score = .5;

    private static List<String> playedSongIds = Arrays.asList("song1", "song2");

    @Test
    public void testRecommendSongNewSong() throws Exception {
        mockStatic(UserController.class);
        PowerMockito.doNothing().when(UserController.class, "incrementNumSongsAdded", any());

        var userMock = mock(User.class);
        var songMock = mock(Song.class);

        when(songMock.getRequestedBy()).thenReturn(new ArrayList<>());
        when(userMock.getUserId()).thenReturn(sessionId);

        var queue = Queue.builder().build();
        queue.recommendSong(songMock, userMock);

        var inOrder = Mockito.inOrder(songMock, userMock);
        inOrder.verify(songMock).getRequestedBy();
        inOrder.verify(userMock).getUserId();
        inOrder.verifyNoMoreInteractions();

        assertEquals(queue.getSongs().size(), 1);
    }

    @Test
    public void testRecommendSongExistingSong() throws Exception {
        mockStatic(UserController.class);
        doNothing().when(UserController.class, "incrementNumSongsAdded", any());

        var userMock = mock(User.class);
        var songMock = mock(Song.class);

        when(songMock.getRequestedBy()).thenReturn(new ArrayList<>());
        when(songMock.getSpotifySongId()).thenReturn("id", "id");
        when(userMock.getUserId()).thenReturn(sessionId);

        var queue = Queue.builder()
                .songs(new ArrayList<>() {{ add(songMock); }})
                .build();

        queue.recommendSong(songMock, userMock);

        var inOrder = Mockito.inOrder(songMock, userMock);
        inOrder.verify(songMock).getRequestedBy();
        inOrder.verify(userMock).getUserId();
        inOrder.verifyNoMoreInteractions();

        assertEquals(queue.getSongs().size(), 1);
    }

    @Test
    public void testAdjustTimeScore() {
        var songMock = mock(Song.class);
        var valueCapture = ArgumentCaptor.forClass(Double.class);

        when(songMock.getTimeAdded()).thenReturn((long)1, (long)5, (long)2);
        doNothing().when(songMock).setTimeScore(anyDouble());

        var queue = Queue.builder()
                .songs(new ArrayList<>() {{ add(songMock); }})
                .build();

        queue.adjustTimeScore();

        var inOrder = Mockito.inOrder(songMock);
        inOrder.verify(songMock, times(3)).getTimeAdded();
        inOrder.verify(songMock).setTimeScore(valueCapture.capture());
        inOrder.verifyNoMoreInteractions();

        assertEquals(.75, valueCapture.getValue(), 0);
    }

    @Test
    public void testAddPlayedSongId() {
        var queue = Queue.builder().build();
        assertEquals(0, queue.getPlayedSongIds().size());
        queue.addPlayedSongId("test");
        assertEquals(1, queue.getPlayedSongIds().size());
    }

    @Test
    public void testGetPlayedSongIdsAsStringEmptyList() {
        var queue = Queue.builder().build();
        assertEquals("", queue.getPlayedSongIdsAsString());
    }

    @Test
    public void testGetPlayedSongIdsAsStringLessThanFive() {
        var songList = Arrays.asList("1", "2", "3", "4");
        var queue = Queue.builder().playedSongIds(songList).build();
        assertEquals("1,2,3,4", queue.getPlayedSongIdsAsString());
    }

    @Test
    public void testGetPlayedSongIdsAsStringMoreThanFive() {
        var songList = Arrays.asList("1", "2", "3", "4", "5", "6");
        var queue = Queue.builder().playedSongIds(songList).build();
        assertEquals("2,3,4,5,6", queue.getPlayedSongIdsAsString());
    }

    @Test
    public void testPopNextSongWithEmptySongList() {
        var queue = Queue.builder().build();
        assertNull(queue.popNextSong());
    }

    @Test
    public void testPopNextSong() {
        var spotifyId = "1";
        var songMock = mock(Song.class);
        when(songMock.getSpotifySongId()).thenReturn(spotifyId);

        var songList = new ArrayList<Song>() {{ add(songMock); }};
        var queue = Queue.builder().songs(songList).build();
        assertEquals(songMock, queue.popNextSong());
        assertEquals(1, queue.getPlayedSongIds().size());
        assertEquals(spotifyId, queue.getPlayedSongIds().get(0));
    }

    @Test
    public void testToDocument() {
        var songDocument = new Document();
        var songMock = mock(Song.class);
        PowerMockito.when(songMock.toDocument()).thenReturn(songDocument);
        var songList = new ArrayList<Song>() {{ add(songMock); }};

        var queueDocFull = Queue.builder()
                .queueId(queueId)
                .playedSongIds(playedSongIds)
                .songs(songList)
                .sessionId(sessionId)
                .score(score)
                .build()
                .toDocument();

        var songsDocs = (List<Document>) queueDocFull.get("songs");
        assertEquals(songsDocs, Collections.singletonList(songDocument));

        var playedSongIdsFromQueue = (List<String>) queueDocFull.get("playedSongIds");
        for (int i = 0; i < playedSongIdsFromQueue.size(); i++) {
            assertEquals(playedSongIdsFromQueue.get(i), playedSongIds.get(i));
        }

        assertEquals(queueDocFull.get("queueId"), queueId.toString());
        assertEquals(queueDocFull.get("sessionId"), sessionId.toString());
        assertEquals(queueDocFull.get("score"), score);

        var queueDocEmpty = Queue.builder()
                .queueId(queueId)
                .build()
                .toDocument();

        assertEquals(queueDocEmpty.get("queueId"), queueId.toString());
        assertNull(queueDocEmpty.get("sessionId"));
        assertEquals(queueDocEmpty.get("playedSongIds"), new ArrayList<Song>());
        assertEquals(queueDocEmpty.get("songs"), new ArrayList<Song>());
        assertEquals(queueDocEmpty.getDouble("score"), 0.0, 0.0);
    }

    @Test
    public void testFromDocument() {
        var songDocument = new Document();
        var songMock = mock(Song.class);
        PowerMockito.when(songMock.toDocument()).thenReturn(songDocument);

        mockStatic(Song.class);
        PowerMockito.when(Song.fromDocument(songDocument)).thenReturn(songMock);
        var songList = new ArrayList<Song>() {{ add(songMock); }};

        var queueDocFull = Queue.builder()
                .playedSongIds(playedSongIds)
                .queueId(queueId)
                .songs(songList)
                .sessionId(sessionId)
                .score(score)
                .build()
                .toDocument();

        var queueFull = Queue.fromDocument(queueDocFull);

        for (int i = 0; i < playedSongIds.size(); i++) {
            assertEquals(playedSongIds.get(i), queueFull.getPlayedSongIds().get(i));
        }

        assertEquals(queueFull.getQueueId(), queueId);
        assertEquals(queueFull.getSessionId(), sessionId);
        assertEquals(queueFull.getScore(), score, .01);
        assertEquals(queueFull.getSongs().get(0), songMock);

        var queueDocEmpty = Queue.builder()
                .queueId(queueId)
                .build()
                .toDocument();

        var queueEmpty = Queue.fromDocument(queueDocEmpty);

        assertEquals(queueEmpty.getQueueId(), queueId);
        assertNull(queueEmpty.getSessionId());
        assertEquals(queueEmpty.getPlayedSongIds(), new ArrayList<String>());
        assertEquals(queueEmpty.getSongs(), new ArrayList<Song>());
        assertEquals(queueEmpty.getScore(), 0.0, 0.0);
    }

    @Test
    public void testGetNumSongsAddedPastHourForUser() {
        var songMock = mock(Song.class);
        var userMock = mock(User.class);

        when(songMock.getTimeAdded()).thenReturn(Instant.now().getEpochSecond(), Instant.now().getEpochSecond() - 5000);
        when(songMock.getRequestedBy()).thenReturn(
                new ArrayList<UUID>() {{ add(sessionId); add(queueId); }},
                new ArrayList<UUID>() {{ add(sessionId); }}
        );


        var songList = new ArrayList<Song>() {{
            add(songMock);
            add(songMock);
        }};

        when(userMock.getUserId()).thenReturn(sessionId);

        var queue = Queue.builder()
                .songs(songList)
                .build();

        var count = queue.getNumSongsAddedPastHourForUser(userMock);

        assertEquals(1, count);
    }

    @Test
    public void testGetSetQueueId() {
        var queue = Queue.builder().queueId(queueId).build();
        assertEquals(queueId, queue.getQueueId());
        queue.setQueueId(sessionId);
        assertEquals(sessionId, queue.getQueueId());
    }

    @Test
    public void testGetSetScore() {
        var queue = Queue.builder().build();
        assertEquals(0, queue.getScore(), 0);
        queue.setScore(score);
        assertEquals(score, queue.getScore(), 0);
    }

    @Test
    public void testGetSetPlayedSongIds() {
        var list = new ArrayList<String>() {{add("wow"); add("ok");}};
        var queue = Queue.builder().build();
        assertEquals(0, queue.getPlayedSongIds().size());
        queue.setPlayedSongIds(list);
        assertEquals(list.size(), queue.getPlayedSongIds().size());
        assertEquals(list.get(0), queue.getPlayedSongIds().get(0));
        assertEquals(list.get(1), queue.getPlayedSongIds().get(1));
    }

    @Test
    public void testGetSetSongs() {
        var list = new ArrayList<Song>() {{ add(Song.builder().build()); }};
        var queue = Queue.builder().build();
        assertEquals(0, queue.getSongs().size());
        queue.setSongs(list);
        assertEquals(list.size(), queue.getSongs().size());
        assertEquals(list.get(0), queue.getSongs().get(0));
    }

    @Test
    public void testGetSetMinTimeLessThanMax() {
        var minTime = -2;
        var queue = Queue.builder().build();
        assertEquals(-1, queue.getMinTime());
        queue.setMinTime(minTime);
        assertEquals(minTime, queue.getMinTime());
    }

    @Test(expected = Exception.class)
    public void testGetSetMinTimeGreaterThanMin() {
        var minTime = 2;
        var queue = Queue.builder().build();
        queue.setMinTime(minTime);
    }

    @Test
    public void testGetSetMaxTimeGreaterThanMin() {
        var maxTime = 10;
        var queue = Queue.builder().build();
        assertEquals(-1, queue.getMaxTime());
        queue.setMaxTime(maxTime);
        assertEquals(maxTime, queue.getMaxTime());
    }

    @Test(expected = Exception.class)
    public void testGetSetMaxTimeLessThanMin() {
        var maxTime = -2;
        var queue = Queue.builder().build();
        queue.setMaxTime(maxTime);
    }

    @Test
    public void testGetSetSessionId() {
        var queue = Queue.builder().build();
        assertNull(queue.getSessionId());
        queue.setSessionId(sessionId);
        assertEquals(sessionId, queue.getSessionId());
    }
}