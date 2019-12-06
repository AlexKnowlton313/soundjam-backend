package tests.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Context;
import jhu.group6.sounDJam.Server;
import jhu.group6.sounDJam.controllers.HealthCheckController;
import jhu.group6.sounDJam.controllers.QueueController;
import jhu.group6.sounDJam.controllers.SettingController;
import jhu.group6.sounDJam.exceptions.InvalidSessionIdException;
import jhu.group6.sounDJam.models.Queue;
import jhu.group6.sounDJam.models.Setting;
import jhu.group6.sounDJam.models.Song;
import jhu.group6.sounDJam.repositories.MongoRepository;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Server.class, QueueController.class, Setting.class})
public class SettingControllerTest {

    @Test(expected = InvalidSessionIdException.class)
    public void testGetSettingNullInContext() {
        PowerMockito.mockStatic(Server.class);

        var contextMock = mock(Context.class);
        var mongoMock = mock(MongoRepository.class);

        when(contextMock.sessionAttribute(anyString())).thenReturn(null);
        when(contextMock.pathParam(anyString())).thenReturn("id");
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(null);

        SettingController.getSetting(contextMock);
    }

    @Test
    public void testGetSettingFromContext() {
        var valueCaptureJson = ArgumentCaptor.forClass(Object.class);
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        PowerMockito.mockStatic(Server.class);

        var contextMock = mock(Context.class);
        var settingMock = mock(Setting.class);

        when(contextMock.sessionAttribute(anyString())).thenReturn(settingMock);

        SettingController.getSetting(contextMock);

        verify(contextMock).status(valueCaptureStatus.capture());
        verify(contextMock).json(valueCaptureJson.capture());

        assertEquals(valueCaptureStatus.getValue(), 200, 0);
        assertEquals(valueCaptureJson.getValue(), settingMock);
    }

    @Test
    public void testGetSettingFromDB() {
        var valueCaptureJson = ArgumentCaptor.forClass(Object.class);
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        PowerMockito.mockStatic(Server.class);

        var contextMock = mock(Context.class);
        var mongoMock = mock(MongoRepository.class);
        var settingMock = mock(Setting.class);

        when(contextMock.sessionAttribute(anyString())).thenReturn(null).thenReturn(settingMock);
        when(contextMock.pathParam(anyString())).thenReturn("id");
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(new Document());

        SettingController.getSetting(contextMock);

        verify(contextMock).status(valueCaptureStatus.capture());
        verify(contextMock).json(valueCaptureJson.capture());

        assertEquals(valueCaptureStatus.getValue(), 200, 0);
        assertEquals(valueCaptureJson.getValue(), settingMock);
    }

    @Test
    public void postSettingAllTrue() throws IOException {
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(QueueController.class);

        var contextMock = mock(Context.class);
        var mongoMock = mock(MongoRepository.class);
        var settingMock = mock(Setting.class);
        var objectMapperMock = mock(ObjectMapper.class);
        var jsonNodeMock = mock(JsonNode.class);
        var queueMock = mock(Queue.class);

        var jsonList = new ArrayList<JsonNode>();
        jsonList.add(jsonNodeMock);

        var songList = new ArrayList<Song>() {{ add(Song.builder().build()); }};

        // getSettingFromContext
        when(contextMock.sessionAttribute(anyString())).thenReturn(null).thenReturn(settingMock);
        when(contextMock.pathParam(anyString())).thenReturn("id");
        PowerMockito.when(Server.getMongoRepository())
                .thenReturn(mongoMock).thenReturn(mongoMock).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(new Document());

        // our method
        PowerMockito.when(Server.getJson()).thenReturn(objectMapperMock);
        when(contextMock.body()).thenReturn("testContextBody");
        when(objectMapperMock.readTree(anyString())).thenReturn(jsonNodeMock);
        when(jsonNodeMock.get(anyString()))
                .thenReturn(jsonNodeMock)
                .thenReturn(jsonNodeMock)
                .thenReturn(jsonNodeMock)
                .thenReturn(jsonNodeMock)
                .thenReturn(jsonNodeMock)
                .thenReturn(jsonNodeMock)
                .thenReturn(jsonNodeMock)
                .thenReturn(jsonNodeMock);
        doNothing().when(settingMock).setMinSongLength(anyInt());
        doNothing().when(settingMock).setMaxSongLength(anyInt());
        doNothing().when(settingMock).setValence(anyDouble());
        doNothing().when(settingMock).setEnergy(anyDouble());
        doNothing().when(settingMock).setDanceability(anyDouble());
        doNothing().when(settingMock).setTempo(anyDouble());
        doNothing().when(settingMock).setMaxUserCanAdd(anyInt());
        doNothing().when(settingMock).setBlacklist(any());
        when(jsonNodeMock.asInt())
                .thenReturn(1)
                .thenReturn(2)
                .thenReturn(7);
        when(jsonNodeMock.asDouble())
                .thenReturn(3.0)
                .thenReturn(4.0)
                .thenReturn(5.0)
                .thenReturn(6.0);
        when(jsonNodeMock.iterator()).thenReturn(jsonList.iterator());
        when(jsonNodeMock.asText()).thenReturn("artist");
        PowerMockito.when(QueueController.getQueueFromContext(any())).thenReturn(queueMock);
        when(queueMock.getSongs()).thenReturn(songList);

        // updateQueueAfterSetting
        PowerMockito.when(QueueController.getQueueFromContext(any())).thenReturn(queueMock);
        PowerMockito.when(QueueController.curateQueue(queueMock, settingMock)).thenReturn(songList);
        doNothing().when(queueMock).setSongs(any());
        when(contextMock.pathParam(anyString())).thenReturn("testString");
        when(queueMock.toDocument()).thenReturn(new Document());
        when(mongoMock.updateOneFromCollectionBySessionId(any(), anyString(), any())).thenReturn(true);

        SettingController.postSetting(contextMock);

        verify(contextMock).status(valueCaptureStatus.capture());
        assertEquals(valueCaptureStatus.getValue(), 204, 0);
    }

    @Test
    public void postSettingAllFalse() throws IOException {
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(QueueController.class);

        var contextMock = mock(Context.class);
        var mongoMock = mock(MongoRepository.class);
        var settingMock = mock(Setting.class);
        var objectMapperMock = mock(ObjectMapper.class);
        var jsonNodeMock = mock(JsonNode.class);
        var queueMock = mock(Queue.class);

        var songList = new ArrayList<Song>();

        // getSettingFromContext
        when(contextMock.sessionAttribute(anyString())).thenReturn(null).thenReturn(settingMock);
        when(contextMock.pathParam(anyString())).thenReturn("id");
        PowerMockito.when(Server.getMongoRepository())
                .thenReturn(mongoMock).thenReturn(mongoMock).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(new Document());

        // our method
        PowerMockito.when(Server.getJson()).thenReturn(objectMapperMock);
        when(contextMock.body()).thenReturn("testContextBody");
        when(objectMapperMock.readTree(anyString())).thenReturn(jsonNodeMock);
        when(jsonNodeMock.get(anyString()))
                .thenReturn(null)
                .thenReturn(null)
                .thenReturn(null)
                .thenReturn(null)
                .thenReturn(null)
                .thenReturn(null)
                .thenReturn(null)
                .thenReturn(null);
        PowerMockito.when(QueueController.getQueueFromContext(any())).thenReturn(queueMock);
        when(queueMock.getSongs()).thenReturn(songList);

        SettingController.postSetting(contextMock);

        verify(contextMock).status(valueCaptureStatus.capture());
        assertEquals(valueCaptureStatus.getValue(), 204, 0);
    }

    @Test
    public void getSettingFromId() {
        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(Setting.class);

        var mongoMock = mock(MongoRepository.class);
        var settingMock = mock(Setting.class);

        var settingDoc = new Document();

        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(settingDoc);
        PowerMockito.when(Setting.fromDocument(settingDoc)).thenReturn(settingMock);

        var ret = SettingController.getSettingFromId("test");

        assertEquals(ret, settingMock);
    }

    @Test(expected = InvalidSessionIdException.class)
    public void getSettingFromIdNull() {
        PowerMockito.mockStatic(Server.class);
        PowerMockito.mockStatic(Setting.class);

        var mongoMock = mock(MongoRepository.class);

        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);
        when(mongoMock.findOneFromCollectionBySessionId(any(), anyString())).thenReturn(null);

        var ret = SettingController.getSettingFromId("test");
    }
}