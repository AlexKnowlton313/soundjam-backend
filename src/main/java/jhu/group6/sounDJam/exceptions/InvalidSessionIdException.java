package jhu.group6.sounDJam.exceptions;

import io.javalin.HttpResponseException;

import java.util.HashMap;
import java.util.UUID;

public class InvalidSessionIdException extends HttpResponseException {
    public InvalidSessionIdException(UUID sessionId) {
        this(sessionId.toString());
    }

    public InvalidSessionIdException(String sessionId) {
        super(400, "No session found with id: " + sessionId, new HashMap<>());
    }
}
