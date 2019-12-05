package jhu.group6.sounDJam.utils;

import io.javalin.Context;
import jhu.group6.sounDJam.exceptions.RecommendLimitReachedException;
import jhu.group6.sounDJam.exceptions.SongBlacklistedException;
import jhu.group6.sounDJam.models.Queue;
import jhu.group6.sounDJam.models.Setting;
import jhu.group6.sounDJam.models.Song;
import jhu.group6.sounDJam.models.User;

import java.io.IOException;

import static jhu.group6.sounDJam.controllers.QueueController.getQueueFromContext;
import static jhu.group6.sounDJam.controllers.SettingController.getSettingFromContext;
import static jhu.group6.sounDJam.controllers.SongController.getSongFromContext;
import static jhu.group6.sounDJam.controllers.UserController.getUserFromContext;
import static jhu.group6.sounDJam.controllers.SessionController.getSessionFromContext;

public class SongRecommendationLimiter {

    public static void ensureUserCanRecommend(Context context) throws IOException {
        if (!context.path().contains("/recommend")) return;

        var user = getUserFromContext(context);
        var setting = getSettingFromContext(context);
        var queue = getQueueFromContext(context);
        var session = getSessionFromContext(context);

        if (!ValidUserHandler.isUserDJForSession(user, session) && !isUserUnderSongLimit(user, setting, queue)) {
            throw new RecommendLimitReachedException(user, setting);
        }

        var song = getSongFromContext(context);

        if (isSongBlacklisted(song, setting)) {
            throw new SongBlacklistedException(song, setting);
        }
    }

    private static boolean isSongBlacklisted(Song song, Setting setting) {
        return setting.getBlacklist().contains(song.getArtist());
    }

    private static boolean isUserUnderSongLimit(User user, Setting setting, Queue queue) {
        var numSongsAddedPastHour = queue.getNumSongsAddedPastHourForUser(user);
        var limit = setting.getMaxUserCanAdd();

        return numSongsAddedPastHour < limit;
    }
}
