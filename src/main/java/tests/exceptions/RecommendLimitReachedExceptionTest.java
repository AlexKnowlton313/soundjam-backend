package tests.exceptions;

import jhu.group6.sounDJam.exceptions.RecommendLimitReachedException;
import jhu.group6.sounDJam.models.Setting;
import jhu.group6.sounDJam.models.User;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;

public class RecommendLimitReachedExceptionTest {

    @Test(expected = RecommendLimitReachedException.class)
    public void testException() {
        var userMock = Mockito.mock(User.class);
        var settingMock = Mockito.mock(Setting.class);

        Mockito.when(userMock.getUserId()).thenReturn(UUID.randomUUID());

        throw new RecommendLimitReachedException(userMock, settingMock);
    }

}