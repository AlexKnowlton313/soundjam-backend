package tests.utils;

import io.javalin.Context;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SongRecommendationLimiterTest {

    @Test
    public void ensureUserCanRecommendWrongPath() {
        var contextMock = mock(Context.class);
        when(contextMock.path())
                .thenReturn("NotWhatWeWant/");

        var inOrder = Mockito.inOrder(contextMock);
        inOrder.verify(contextMock).path();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void isSongBlacklisted() {
    }

    @Test
    public void isUserUnderSongLimit() {
    }
}