package tests.utils;

import io.javalin.Context;
import jhu.group6.sounDJam.controllers.*;
import jhu.group6.sounDJam.exceptions.NotValidUserException;
import jhu.group6.sounDJam.exceptions.RecommendLimitReachedException;
import jhu.group6.sounDJam.models.Queue;
import jhu.group6.sounDJam.models.Session;
import jhu.group6.sounDJam.models.Setting;
import jhu.group6.sounDJam.models.User;
import jhu.group6.sounDJam.utils.ValidUserHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        UserController.class,
        SessionController.class
})
public class ValidUserHandlerTest {
    @Test
    public void testEnsureValidUserForSessionLoggedOut() {
        var contextMock = mock(Context.class);
        when(contextMock.path()).thenReturn("/user");
        when(contextMock.method()).thenReturn("POST");

        ValidUserHandler.ensureValidUserForSession(contextMock);
    }

    @Test(expected = NotValidUserException.class)
    public void testEnsureValidUserForSessionNotValidUser() throws Exception {
        var userId = UUID.randomUUID();

        var contextMock = mock(Context.class);
        when(contextMock.path()).thenReturn("/ugh");
        when(contextMock.method()).thenReturn("GET");

        PowerMockito.mockStatic(UserController.class);
        PowerMockito.mockStatic(SessionController.class);

        var userMock = Mockito.mock(User.class);
        var sessionMock = Mockito.mock(Session.class);

        PowerMockito.doReturn(userMock).when(UserController.class, "getUserFromContext", contextMock);
        PowerMockito.doReturn(sessionMock).when(SessionController.class, "getSessionFromContext", contextMock);

        when(sessionMock.getPartierIds()).thenReturn(new ArrayList<UUID>() {{ add(UUID.randomUUID()); }});
        when(userMock.getUserId()).thenReturn(userId);
        when(sessionMock.getDjId()).thenReturn(UUID.randomUUID());
        when(userMock.getUserId()).thenReturn(userId); // for exception
        when(sessionMock.getSessionId()).thenReturn(userId); // for exception

        ValidUserHandler.ensureValidUserForSession(contextMock);

        var inOrder = Mockito.inOrder(contextMock, sessionMock, userMock);
        inOrder.verify(contextMock).method();
        inOrder.verify(sessionMock).getPartierIds();
        inOrder.verify(userMock).getUserId();
        inOrder.verify(sessionMock).getDjId();
        inOrder.verify(userMock).getUserId();
        inOrder.verify(sessionMock).getSessionId();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testEnsureValidUserForSession() throws Exception {
        var userId = UUID.randomUUID();

        var contextMock = mock(Context.class);
        when(contextMock.path()).thenReturn("/ugh");

        PowerMockito.mockStatic(UserController.class);
        PowerMockito.mockStatic(SessionController.class);

        var userMock = Mockito.mock(User.class);
        var sessionMock = Mockito.mock(Session.class);

        PowerMockito.doReturn(userMock).when(UserController.class, "getUserFromContext", contextMock);
        PowerMockito.doReturn(sessionMock).when(SessionController.class, "getSessionFromContext", contextMock);

        when(sessionMock.getPartierIds()).thenReturn(new ArrayList<UUID>() {{ add(userId); }});
        when(userMock.getUserId()).thenReturn(userId);
        when(sessionMock.getDjId()).thenReturn(UUID.randomUUID());

        ValidUserHandler.ensureValidUserForSession(contextMock);
    }

    @Test
    public void testEnsureValidDJForSessionLoggedOut() {
        var contextMock = mock(Context.class);
        when(contextMock.path()).thenReturn("/user");
        when(contextMock.method()).thenReturn("POST");

        ValidUserHandler.ensureValidDJForSession(contextMock);
    }

    @Test
    public void testEnsureValidDJForSessionForPartier() {
        var contextMock = mock(Context.class);
        when(contextMock.path()).thenReturn("/boo");
        when(contextMock.method()).thenReturn("POST");

        ValidUserHandler.ensureValidDJForSession(contextMock);
    }

    @Test(expected = NotValidUserException.class)
    public void testEnsureValidDJForSessionIsNotDJ() throws Exception {
        var contextMock = mock(Context.class);
        when(contextMock.path()).thenReturn("/anything");
        when(contextMock.method()).thenReturn("POST");

        PowerMockito.mockStatic(UserController.class);
        PowerMockito.mockStatic(SessionController.class);

        var userMock = Mockito.mock(User.class);
        var sessionMock = Mockito.mock(Session.class);

        PowerMockito.doReturn(userMock).when(UserController.class, "getUserFromContext", contextMock);
        PowerMockito.doReturn(sessionMock).when(SessionController.class, "getSessionFromContext", contextMock);

        when(sessionMock.getDjId()).thenReturn(UUID.randomUUID());
        when(userMock.getUserId()).thenReturn(UUID.randomUUID());
        when(sessionMock.getSessionId()).thenReturn(UUID.randomUUID());
        when(userMock.getUserId()).thenReturn(UUID.randomUUID());

        ValidUserHandler.ensureValidDJForSession(contextMock);
    }

    @Test
    public void testEnsureValidDJForSessionIsDJ() throws Exception {
        var djId = UUID.randomUUID();

        var contextMock = mock(Context.class);
        when(contextMock.path()).thenReturn("/anything");
        when(contextMock.method()).thenReturn("POST");

        PowerMockito.mockStatic(UserController.class);
        PowerMockito.mockStatic(SessionController.class);

        var userMock = Mockito.mock(User.class);
        var sessionMock = Mockito.mock(Session.class);

        PowerMockito.doReturn(userMock).when(UserController.class, "getUserFromContext", contextMock);
        PowerMockito.doReturn(sessionMock).when(SessionController.class, "getSessionFromContext", contextMock);

        when(sessionMock.getDjId()).thenReturn(djId);
        when(userMock.getUserId()).thenReturn(djId);

        ValidUserHandler.ensureValidDJForSession(contextMock);
    }
}