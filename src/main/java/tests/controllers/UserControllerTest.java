package tests.controllers;

import io.javalin.BadRequestResponse;
import io.javalin.Context;
import jhu.group6.sounDJam.Server;
import jhu.group6.sounDJam.controllers.UserController;
import jhu.group6.sounDJam.models.User;
import jhu.group6.sounDJam.repositories.MongoRepository;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Server.class, User.class})
public class UserControllerTest {

    @Test(expected = BadRequestResponse.class)
    public void testGetUserFromContextWhereNullUserid() {
        var map = new HashMap<String, String>();
        map.put("Userid", "id");

        var mongoMock = mock(MongoRepository.class);
        when(mongoMock.findOneFromCollection(any(), any())).thenReturn(null);

        PowerMockito.mockStatic(Server.class);
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);

        var contextMock = mock(Context.class);
        when(contextMock.sessionAttribute(anyString())).thenReturn(null);
        when(contextMock.headerMap()).thenReturn(map);

        UserController.getUserFromContext(contextMock);
    }

    @Test
    public void testGetUserFromContextWhereNulluserid() {
        var map = new HashMap<String, String>();
        map.put("userid", "id");

        var userMock = mock(User.class);
        var mongoMock = mock(MongoRepository.class);
        when(mongoMock.findOneFromCollection(any(), any())).thenReturn(new Document());

        PowerMockito.mockStatic(Server.class);
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);

        PowerMockito.mockStatic(User.class);
        PowerMockito.when(User.fromDocument(any())).thenReturn(userMock);

        var contextMock = mock(Context.class);
        when(contextMock.sessionAttribute(anyString())).thenReturn(null).thenReturn(userMock);
        when(contextMock.headerMap()).thenReturn(map);

        assertEquals(UserController.getUserFromContext(contextMock), userMock);
    }

    @Test
    public void testGetUserFromContextWhereNulluserId() {
        var map = new HashMap<String, String>();
        map.put("userId", "id");

        var userMock = mock(User.class);
        var mongoMock = mock(MongoRepository.class);
        when(mongoMock.findOneFromCollection(any(), any())).thenReturn(new Document());

        PowerMockito.mockStatic(Server.class);
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);

        PowerMockito.mockStatic(User.class);
        PowerMockito.when(User.fromDocument(any())).thenReturn(userMock);

        var contextMock = mock(Context.class);
        when(contextMock.sessionAttribute(anyString())).thenReturn(null).thenReturn(userMock);
        when(contextMock.headerMap()).thenReturn(map);

        assertEquals(UserController.getUserFromContext(contextMock), userMock);
    }

    @Test
    public void testGetUserFromContextWhereNotNullUser() {
        var userMock = mock(User.class);

        var contextMock = mock(Context.class);
        when(contextMock.sessionAttribute(anyString())).thenReturn(userMock).thenReturn(userMock);

        assertEquals(UserController.getUserFromContext(contextMock), userMock);
    }

    @Test
    public void incrementNumSongsAdded() {
        var userMock = mock(User.class);
        var mongoMock = mock(MongoRepository.class);
        when(mongoMock.updateUser(any())).thenReturn(true);

        PowerMockito.mockStatic(Server.class);
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);

        UserController.incrementNumSongsAdded(userMock);

        var inOrder = Mockito.inOrder(userMock, mongoMock);
        inOrder.verify(userMock).incrementNumSongsAdded();
        inOrder.verify(mongoMock).updateUser(any());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void incrementNumBoos() {
        var userMock = mock(User.class);
        var mongoMock = mock(MongoRepository.class);
        when(mongoMock.updateUser(any())).thenReturn(true);

        PowerMockito.mockStatic(Server.class);
        PowerMockito.when(Server.getMongoRepository()).thenReturn(mongoMock);

        UserController.incrementNumBoos(userMock);

        var inOrder = Mockito.inOrder(userMock, mongoMock);
        inOrder.verify(userMock).incrementNumBoos();
        inOrder.verify(mongoMock).updateUser(any());
        inOrder.verifyNoMoreInteractions();
    }
}