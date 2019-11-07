package jhu.group6.sounDJam.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.Context;
import jhu.group6.sounDJam.Server;
import jhu.group6.sounDJam.exceptions.InvalidSessionIdException;
import jhu.group6.sounDJam.models.Queue;
import jhu.group6.sounDJam.models.Setting;
import jhu.group6.sounDJam.utils.CollectionNames;

import java.io.IOException;
import java.util.ArrayList;

import static jhu.group6.sounDJam.controllers.QueueController.curateQueue;
import static jhu.group6.sounDJam.controllers.QueueController.getQueueFromContext;

public class SettingController {
    public static void getSetting(Context context) {
        var setting = getSettingFromContext(context);
        context.json(setting);
        context.status(200);
    }

    public static void postSetting(Context context) throws IOException {
        var setting = getSettingFromContext(context);
        var newSettingParams = Server.getJson().readTree(context.body());

        var minSongLength = newSettingParams.get("minTimeLimit");
        var maxSongLength = newSettingParams.get("maxTimeLimit");
        var valence = newSettingParams.get("valence");
        var energy = newSettingParams.get("energy");
        var danceability = newSettingParams.get("danceability");
        var tempo = newSettingParams.get("tempo");
        var maxUserCanAdd = newSettingParams.get("maxUserCanAdd");
        var blacklistNodeList = newSettingParams.get("blacklist");
        if (minSongLength != null) {
            setting.setMinSongLength(minSongLength.asInt());
        }
        if (maxSongLength != null) {
            setting.setMaxSongLength(maxSongLength.asInt());
        }
        if (valence != null) {
            setting.setValence(valence.asDouble());
        }
        if (energy != null) {
            setting.setEnergy(energy.asDouble());
        }
        if (danceability != null) {
            setting.setDanceability(danceability.asDouble());
        }
        if (tempo != null) {
            setting.setTempo(tempo.asDouble());
        }
        if (maxUserCanAdd != null) {
            setting.setMaxUserCanAdd(maxUserCanAdd.asInt());
        }
        if (blacklistNodeList != null) {
            var blacklistStringList = new ArrayList<String>();
            for (final JsonNode artist : blacklistNodeList) {
                blacklistStringList.add(artist.asText());
            }
            setting.setBlacklist(blacklistStringList);
        }


        if (getQueueFromContext(context).getSongs().size() != 0) {
            updateQueueAfterSetting(context, setting);
        }

        Server.getMongoRepository().updateOneFromCollectionBySessionId(
                CollectionNames.SETTING,
                context.pathParam("session-id"),
                setting.toDocument());

        context.status(204);
    }

    public static Setting getSettingFromContext(Context context) {
        if (context.sessionAttribute("setting") == null) {
            var sessionId = context.pathParam("session-id");
            var settingDoc = Server.getMongoRepository().findOneFromCollectionBySessionId(CollectionNames.SETTING, sessionId);
            if (settingDoc == null) throw new InvalidSessionIdException(sessionId);
            context.sessionAttribute("setting", Setting.fromDocument(settingDoc));
        }

        return context.sessionAttribute("setting");
    }

    public static Setting getSettingFromId(String sessionId) {
        var settingDoc = Server.getMongoRepository().findOneFromCollectionBySessionId(CollectionNames.SETTING, sessionId);
        if (settingDoc == null) throw new InvalidSessionIdException(sessionId);
        return Setting.fromDocument(settingDoc);
    }
  
    private static void updateQueueAfterSetting(Context context, Setting setting) {
        Queue queue = getQueueFromContext(context);
        queue.setSongs(curateQueue(queue, setting));

        Server.getMongoRepository().updateOneFromCollectionBySessionId(
                CollectionNames.QUEUE,
                context.pathParam("session-id"),
                queue.toDocument());
    }
}