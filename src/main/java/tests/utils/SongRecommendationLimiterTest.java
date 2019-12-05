package tests.utils;

import io.javalin.Context;
import jhu.group6.sounDJam.controllers.*;
import jhu.group6.sounDJam.exceptions.RecommendLimitReachedException;
import jhu.group6.sounDJam.exceptions.SongBlacklistedException;
import jhu.group6.sounDJam.models.*;
import jhu.group6.sounDJam.utils.SongRecommendationLimiter;
import jhu.group6.sounDJam.utils.ValidUserHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        UserController.class,
        SettingController.class,
        QueueController.class,
        SongController.class,
        SessionController.class,
        ValidUserHandler.class,
        RecommendLimitReachedException.class
})
public class SongRecommendationLimiterTest {

    @Test
    public void testEnsureUserCanRecommendWrongPath() throws IOException {
        var contextMock = mock(Context.class);
        when(contextMock.path()).thenReturn("NotWhatWeWant/");

        SongRecommendationLimiter.ensureUserCanRecommend(contextMock);

        var inOrder = Mockito.inOrder(contextMock);
        inOrder.verify(contextMock).path();
        inOrder.verifyNoMoreInteractions();
    }

    @Test(expected = RecommendLimitReachedException.class)
    public void testEnsureUserCanRecommendInvalidUserThrowsException() throws Exception {
        var contextMock = mock(Context.class);
        Mockito.when(contextMock.path()).thenReturn("test/recommend");

        PowerMockito.mockStatic(UserController.class);
        PowerMockito.mockStatic(SessionController.class);
        PowerMockito.mockStatic(QueueController.class);
        PowerMockito.mockStatic(SongController.class);
        PowerMockito.mockStatic(SettingController.class);

        PowerMockito.mockStatic(ValidUserHandler.class);

        var userMock = Mockito.mock(User.class);
        var sessionMock = Mockito.mock(Session.class);
        var queueMock = Mockito.mock(Queue.class);
        var settingMock = Mockito.mock(Setting.class);

        PowerMockito.doReturn(userMock).when(UserController.class, "getUserFromContext", contextMock);
        PowerMockito.doReturn(sessionMock).when(SessionController.class, "getSessionFromContext", contextMock);
        PowerMockito.doReturn(queueMock).when(QueueController.class, "getQueueFromContext", contextMock);
        PowerMockito.doReturn(settingMock).when(SettingController.class, "getSettingFromContext", contextMock);

        PowerMockito.doReturn(Boolean.FALSE).when(ValidUserHandler.class, "isUserDJForSession", userMock, sessionMock);
        Mockito.when(queueMock.getNumSongsAddedPastHourForUser(userMock)).thenReturn((long) 10);
        Mockito.when(userMock.getNumSongsAdded()).thenReturn(11);
        Mockito.when(userMock.getUserId()).thenReturn(UUID.randomUUID());// this is for the expeption

        SongRecommendationLimiter.ensureUserCanRecommend(contextMock);

        var inOrder = Mockito.inOrder(contextMock, queueMock, userMock);
        inOrder.verify(contextMock).path();
        inOrder.verify(queueMock).getNumSongsAddedPastHourForUser(userMock);
        inOrder.verify(userMock).getNumSongsAdded();
        inOrder.verify(userMock).getUserId();
        inOrder.verifyNoMoreInteractions();
    }

    @Test(expected = SongBlacklistedException.class)
    public void testEnsureUserCanRecommendBlacklistSongThrowsException() throws Exception {
        var contextMock = mock(Context.class);
        Mockito.when(contextMock.path()).thenReturn("test/recommend");

        PowerMockito.mockStatic(UserController.class);
        PowerMockito.mockStatic(SessionController.class);
        PowerMockito.mockStatic(QueueController.class);
        PowerMockito.mockStatic(SongController.class);
        PowerMockito.mockStatic(SettingController.class);

        PowerMockito.mockStatic(ValidUserHandler.class);

        var userMock = Mockito.mock(User.class);
        var sessionMock = Mockito.mock(Session.class);
        var queueMock = Mockito.mock(Queue.class);
        var songMock = Mockito.mock(Song.class);
        var settingMock = Mockito.mock(Setting.class);

        PowerMockito.doReturn(userMock).when(UserController.class, "getUserFromContext", contextMock);
        PowerMockito.doReturn(sessionMock).when(SessionController.class, "getSessionFromContext", contextMock);
        PowerMockito.doReturn(queueMock).when(QueueController.class, "getQueueFromContext", contextMock);
        PowerMockito.doReturn(songMock).when(SongController.class, "getSongFromContext", contextMock);
        PowerMockito.doReturn(settingMock).when(SettingController.class, "getSettingFromContext", contextMock);

        PowerMockito.doReturn(Boolean.TRUE).when(ValidUserHandler.class, "isUserDJForSession", userMock, sessionMock);
        Mockito.when(queueMock.getNumSongsAddedPastHourForUser(userMock)).thenReturn((long) 10);
        Mockito.when(userMock.getNumSongsAdded()).thenReturn(9);
        Mockito.when(settingMock.getBlacklist()).thenReturn(new ArrayList<>() {{ add("testSong"); }});
        Mockito.when(songMock.getArtist()).thenReturn("testSong");
        Mockito.when(songMock.getSpotifySongId()).thenReturn("TEST SPOTIFY ID"); // this is for the exception

        SongRecommendationLimiter.ensureUserCanRecommend(contextMock);

        var inOrder = Mockito.inOrder(contextMock, queueMock, userMock);
        inOrder.verify(contextMock).path();
        inOrder.verify(queueMock).getNumSongsAddedPastHourForUser(userMock);
        inOrder.verify(userMock).getNumSongsAdded();
        inOrder.verify(settingMock).getBlacklist();
        inOrder.verify(songMock).getArtist();
        inOrder.verify(songMock).getSpotifySongId();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testEnsureUserCanRecommendPasses() throws Exception {
        var contextMock = mock(Context.class);
        Mockito.when(contextMock.path()).thenReturn("test/recommend");

        PowerMockito.mockStatic(UserController.class);
        PowerMockito.mockStatic(SessionController.class);
        PowerMockito.mockStatic(QueueController.class);
        PowerMockito.mockStatic(SongController.class);
        PowerMockito.mockStatic(SettingController.class);

        PowerMockito.mockStatic(ValidUserHandler.class);

        var userMock = Mockito.mock(User.class);
        var sessionMock = Mockito.mock(Session.class);
        var queueMock = Mockito.mock(Queue.class);
        var songMock = Mockito.mock(Song.class);
        var settingMock = Mockito.mock(Setting.class);

        PowerMockito.doReturn(userMock).when(UserController.class, "getUserFromContext", contextMock);
        PowerMockito.doReturn(sessionMock).when(SessionController.class, "getSessionFromContext", contextMock);
        PowerMockito.doReturn(queueMock).when(QueueController.class, "getQueueFromContext", contextMock);
        PowerMockito.doReturn(songMock).when(SongController.class, "getSongFromContext", contextMock);
        PowerMockito.doReturn(settingMock).when(SettingController.class, "getSettingFromContext", contextMock);

        PowerMockito.doReturn(Boolean.TRUE).when(ValidUserHandler.class, "isUserDJForSession", userMock, sessionMock);
        Mockito.when(queueMock.getNumSongsAddedPastHourForUser(userMock)).thenReturn((long) 10);
        Mockito.when(userMock.getNumSongsAdded()).thenReturn(9);
        Mockito.when(settingMock.getBlacklist()).thenReturn(new ArrayList<>() {{ add("testSong"); }});
        Mockito.when(songMock.getArtist()).thenReturn("a different artist!");

        SongRecommendationLimiter.ensureUserCanRecommend(contextMock);
    }
}