package jhu.group6.sounDJam.utils;

import io.javalin.Context;
import jhu.group6.sounDJam.exceptions.NotValidUserException;
import jhu.group6.sounDJam.models.Session;
import jhu.group6.sounDJam.models.User;

import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static jhu.group6.sounDJam.controllers.SessionController.getSessionFromContext;
import static jhu.group6.sounDJam.controllers.UserController.getUserFromContext;

public class ValidUserHandler {
    public static void ensureValidUserForSession (Context context) {
        if (isLoggedOutEndpoint(context)) {
            return;
        }

        Session session;
        User user;

        session = getSessionFromContext(context);
        user = getUserFromContext(context);

        if (!isUserPartierForSession(user, session) && !isUserDJForSession(user, session)) {
            throw new NotValidUserException(user, session);
        }
    }

    public static void ensureValidDJForSession (Context context) {
        if (isLoggedOutEndpoint(context) || isPartierEndpoint(context)) {
            return;
        }

        var session = getSessionFromContext(context);
        var user = getUserFromContext(context);

        if (!isUserDJForSession(user, session)) {
            throw new NotValidUserException(user, session);
        }
    }

    private static boolean isLoggedOutEndpoint(Context context) {
        return context.path().contains("/user") && context.method().equals("POST");
    }

    private static boolean isPartierEndpoint(Context context) {
        return context.path().contains("/boo") ||
                context.path().contains("/search") ||
                context.path().contains("/recommend") ||
                context.path().contains("/song");
    }

    private static boolean isUserPartierForSession(User user, Session session) {
        var partiers = session.getPartierIds().stream().map(UUID::toString).collect(toList());
        return partiers.contains(user.getUserId().toString());
    }

    static boolean isUserDJForSession(User user, Session session) {
        return session.getDjId().toString().equals(user.getUserId().toString());
    }
}
