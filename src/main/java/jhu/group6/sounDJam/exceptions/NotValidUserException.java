package jhu.group6.sounDJam.exceptions;

import io.javalin.HttpResponseException;
import jhu.group6.sounDJam.models.Session;
import jhu.group6.sounDJam.models.User;

import java.util.HashMap;
import java.util.UUID;

public class NotValidUserException extends HttpResponseException {
    public NotValidUserException(User user, Session session) {
        this(user.getUserId(), session.getSessionId());
    }

    public NotValidUserException(UUID userId, UUID sessionId) {
        this(userId.toString(), sessionId.toString());
    }

    public NotValidUserException(String userId, String sessionId) {
        super(401, "User " + userId + " is not a member of Session " + sessionId, new HashMap<>());
    }
}
