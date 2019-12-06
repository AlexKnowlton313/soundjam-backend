package tests.controllers;

import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import io.javalin.Context;
import jhu.group6.sounDJam.Server;
import jhu.group6.sounDJam.controllers.*;
import jhu.group6.sounDJam.exceptions.InvalidSessionIdException;
import jhu.group6.sounDJam.models.Queue;
import jhu.group6.sounDJam.models.Setting;
import jhu.group6.sounDJam.models.Song;
import jhu.group6.sounDJam.models.User;
import jhu.group6.sounDJam.repositories.MongoRepository;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        Server.class,
        Queue.class,
        UserController.class,
        SongController.class,
        SettingController.class,
        SpotifyController.class
})
public class QueueControllerTest {

    @Test
    public void getQueueWhenQueueIsInContext() {
        var valueCaptureJson = ArgumentCaptor.forClass(Object.class);
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        PowerMockito.mockStatic(Server.class);

        var contextMock = mock(Context.class);
        var queueMock = mock(Queue.class);

        var songs = new ArrayList<Song>() {{ add(null); }};

        when(contextMock.sessionAttribute(anyString()))
                .thenReturn(queueMock)
                .thenReturn(queueMock);
        when(queueMock.getSongs()).thenReturn(songs);

        QueueController.getQueue(contextMock);

        verify(contextMock).status(valueCaptureStatus.capture());
        verify(contextMock).json(valueCaptureJson.capture());

        assertEquals(valueCaptureStatus.getValue(), 200, 0);
        assertEquals(valueCaptureJson.getValue(), songs);
    }

    @Test
    public void getQueueWhenQueueIsNotInContextButSessionIdIsAndNotNull() {
        var valueCaptureJson = ArgumentCaptor.forClass(Object.class);
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(Queue.class);

        var contextMock = mock(Context.class);
        var mongoMock = mock(MongoRepository.class);
        var queueMock = mock(Queue.class);

        var songs = new ArrayList<Song>() {{ add(null); }};
        var queueDoc = new Document();

        when(contextMock.sessionAttribute(anyString()))
                .thenReturn(null)
                .thenReturn(queueMock);
        when(contextMock.pathParam(anyString())).thenReturn("sessionId");
        when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(queueDoc);
        when(Queue.fromDocument(queueDoc)).thenReturn(queueMock);
        doNothing().when(contextMock).sessionAttribute(anyString(), any());
        when(queueMock.getSongs()).thenReturn(songs);

        QueueController.getQueue(contextMock);

        verify(contextMock).status(valueCaptureStatus.capture());
        verify(contextMock).json(valueCaptureJson.capture());

        assertEquals(valueCaptureStatus.getValue(), 200, 0);
        assertEquals(valueCaptureJson.getValue(), songs);
    }

    @Test(expected = InvalidSessionIdException.class)
    public void getQueueWhenQueueIsNotInContextButSessionIdIsAndNull() {
        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(Queue.class);

        var contextMock = mock(Context.class);
        var mongoMock = mock(MongoRepository.class);
        var queueMock = mock(Queue.class);

        when(contextMock.sessionAttribute(anyString()))
                .thenReturn(null)
                .thenReturn(queueMock);
        when(contextMock.pathParam(anyString())).thenReturn("sessionId");
        when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(null);

        QueueController.getQueue(contextMock);
    }

    @Test
    public void curateQueue() {
        var settingMock = mock(Setting.class);
        var queueMock = mock(Queue.class);
        var songMock = mock(Song.class);
        var anotherSongMock = mock(Song.class);

        var songListMock = new ArrayList<Song>() {{
            add(songMock);
            add(anotherSongMock);
        }};

        // curate queue
        doNothing().when(queueMock).adjustTimeScore();
        when(queueMock.getSongs()).thenReturn(songListMock).thenReturn(songListMock);

        // create graph
        when(songMock.score(settingMock)).thenReturn(100.0);
        when(anotherSongMock.score(settingMock)).thenReturn(20.0);
        doNothing().when(songMock).setScore(anyDouble());
        doNothing().when(anotherSongMock).setScore(anyDouble());
        when(songMock.score(anotherSongMock)).thenReturn(15.0);
        when(anotherSongMock.score(songMock)).thenReturn(10.0);
        when(songMock.getScore()).thenReturn(100.0);
        when(anotherSongMock.getScore()).thenReturn(20.0);

        // parse graph
        // nothing to do here actually

        // get closest song
        // nothing to do here either

        var ret = QueueController.curateQueue(queueMock, settingMock);

        assertEquals(ret.get(0), songMock);
        assertEquals(ret.get(1), anotherSongMock);
    }

    @Test
    public void recommendSong() throws IOException {
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        PowerMockito.mockStatic(UserController.class);
        PowerMockito.mockStatic(SongController.class);
        PowerMockito.mockStatic(SettingController.class);
        PowerMockito.mockStatic(SpotifyController.class);
        PowerMockito.mockStatic(Server.class);

        var contextMock = mock(Context.class);
        var userMock = mock(User.class);
        var songMock = mock(Song.class);
        var anotherSongMock = mock(Song.class);
        var settingMock = mock(Setting.class);
        var audioFeaturesMock = mock(AudioFeatures.class);
        var queueMock = mock(Queue.class);
        var mongoMock = mock(MongoRepository.class);

        var songListMock = new ArrayList<Song>() {{
            add(songMock);
            add(anotherSongMock);
        }};

        PowerMockito.when(UserController.getUserFromContext(contextMock)).thenReturn(userMock);
        PowerMockito.when(SongController.getSongFromContext(contextMock)).thenReturn(songMock);
        PowerMockito.when(SettingController.getSettingFromContext(contextMock)).thenReturn(settingMock);
        when(songMock.getSpotifySongId()).thenReturn("spotifySongId");
        PowerMockito.when(SpotifyController.getAudioFeaturesFromSongId(contextMock, "spotifySongId")).thenReturn(audioFeaturesMock);
        doNothing().when(songMock).setAudioFeatures(audioFeaturesMock);
        doNothing().when(queueMock).recommendSong(songMock, userMock);
        doNothing().when(queueMock).setSongs(any());
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(contextMock.pathParam(anyString())).thenReturn("session-id");
        when(queueMock.toDocument()).thenReturn(new Document());
        when(mongoMock.updateOneFromCollectionBySessionId(any(), anyString(), any())).thenReturn(true);


        // curate queue
        doNothing().when(queueMock).adjustTimeScore();
        when(queueMock.getSongs()).thenReturn(songListMock).thenReturn(songListMock);

        // getQueueFromContext()
        when(contextMock.sessionAttribute(anyString())).thenReturn(queueMock).thenReturn(queueMock);

        QueueController.recommendSong(contextMock);

        verify(contextMock).status(valueCaptureStatus.capture());
        assertEquals(valueCaptureStatus.getValue(), 202, 0);
    }

    @Test
    public void getGraph() {
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        PowerMockito.mockStatic(SettingController.class);

        var contextMock = mock(Context.class);
        var settingMock = mock(Setting.class);
        var queueMock = mock(Queue.class);
        var songMock = mock(Song.class);
        var anotherSongMock = mock(Song.class);

        var songListMock = new ArrayList<Song>() {{
            add(songMock);
            add(anotherSongMock);
        }};

        PowerMockito.when(SettingController.getSettingFromContext(contextMock)).thenReturn(settingMock);
        when(queueMock.getSongs()).thenReturn(songListMock);
        when(songMock.getName()).thenReturn("songName");


        // getQueueFromContext()
        when(contextMock.sessionAttribute(anyString())).thenReturn(queueMock).thenReturn(queueMock);

        // create graph
        when(songMock.score(settingMock)).thenReturn(100.0);
        when(anotherSongMock.score(settingMock)).thenReturn(20.0);
        doNothing().when(songMock).setScore(anyDouble());
        doNothing().when(anotherSongMock).setScore(anyDouble());
        when(songMock.score(anotherSongMock)).thenReturn(15.0);
        when(anotherSongMock.score(songMock)).thenReturn(10.0);
        when(songMock.getScore()).thenReturn(100.0);
        when(anotherSongMock.getScore()).thenReturn(20.0);

        QueueController.getGraph(contextMock);

        verify(contextMock).status(valueCaptureStatus.capture());
        assertEquals(valueCaptureStatus.getValue(), 200, 0);
    }
}