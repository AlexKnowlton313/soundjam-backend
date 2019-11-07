package jhu.group6.sounDJam.controllers;

import io.javalin.BadRequestResponse;
import io.javalin.Context;
import jhu.group6.sounDJam.Server;
import jhu.group6.sounDJam.models.User;
import jhu.group6.sounDJam.utils.CollectionNames;
import org.bson.Document;

public class UserController {
    public static User getUserFromContext(Context context) {
        if (context.sessionAttribute("user") == null) {
            var userId = context.headerMap().get("Userid");
            if (userId == null) {
                userId = context.headerMap().get("userid");
            }
            if (userId == null) {
                userId = context.headerMap().get("userId");
            }
            context.sessionAttribute("user", getUserFromId(userId));
        }

        return context.sessionAttribute("user");
    }

    public static User getUserFromId(String userId) {
        var searchParams = new Document("userId", userId);
        var userDoc = Server.getMongoRepository().findOneFromCollection(CollectionNames.USER, searchParams);
        if (userDoc == null) throw new BadRequestResponse("Couldn't find user with id: " + userId);
        return User.fromDocument(userDoc);
    }

    public static void incrementNumSongsAdded(User user) {
        user.incrementNumSongsAdded();
        Server.getMongoRepository().updateUser(user.toDocument());
    }

    public static void incrementNumBoos(User user) {
        user.incrementNumBoos();
        Server.getMongoRepository().updateUser(user.toDocument());
    }
}
