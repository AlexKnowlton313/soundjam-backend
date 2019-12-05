package tests.controllers;

import io.javalin.Context;
import jhu.group6.sounDJam.Server;
import jhu.group6.sounDJam.controllers.HealthCheckController;
import jhu.group6.sounDJam.controllers.SettingController;
import jhu.group6.sounDJam.exceptions.InvalidSessionIdException;
import jhu.group6.sounDJam.models.Setting;
import jhu.group6.sounDJam.models.Song;
import jhu.group6.sounDJam.repositories.MongoRepository;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Server.class})
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
    public void postSetting() {
    }

    @Test
    public void getSettingFromContext() {
    }

    @Test
    public void getSettingFromId() {
    }
}