package tests.exceptions;

import jhu.group6.sounDJam.exceptions.InvalidSessionIdException;
import org.junit.Test;

import java.util.UUID;

public class InvalidSessionIdExceptionTest {
    @Test(expected = InvalidSessionIdException.class)
    public void testException() {
        var sessionId = UUID.randomUUID();

        throw new InvalidSessionIdException(sessionId);
    }
}