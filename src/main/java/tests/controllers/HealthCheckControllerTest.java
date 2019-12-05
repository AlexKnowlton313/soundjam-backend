package tests.controllers;

import io.javalin.Context;
import jhu.group6.sounDJam.controllers.HealthCheckController;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class HealthCheckControllerTest {

    @Test
    public void healthCheck() {
        var valueCaptureString = ArgumentCaptor.forClass(String.class);
        var valueCaptureStatus = ArgumentCaptor.forClass(Integer.class);

        var contextMock = mock(Context.class);

        HealthCheckController.healthCheck(contextMock);

        verify(contextMock).status(valueCaptureStatus.capture());
        verify(contextMock).result(valueCaptureString.capture());

        assertEquals(valueCaptureStatus.getValue(), 200, 0);
        assertEquals(valueCaptureString.getValue(), "ok!");
    }
}