package jhu.group6.sounDJam.exceptions;

import io.javalin.HttpResponseException;
import jhu.group6.sounDJam.models.Setting;
import jhu.group6.sounDJam.models.User;

import java.util.HashMap;
import java.util.UUID;

public class RecommendLimitReachedException extends HttpResponseException {
    public RecommendLimitReachedException(User user, Setting setting) {
        this(user.getUserId(), setting);
    }

    public RecommendLimitReachedException(UUID userId, Setting setting) {
        this(userId.toString(), setting);
    }

    public RecommendLimitReachedException(String userId, Setting setting) {
        super(401, "You've reached your recommendation limit for the hour! Please try again later.", new HashMap<>());
    }
}
