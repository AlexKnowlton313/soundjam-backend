package jhu.group6.sounDJam.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.javalin.BadRequestResponse;
import io.javalin.Context;
import jhu.group6.sounDJam.Server;
import jhu.group6.sounDJam.models.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class SongController {
    public static Song getSongFromContext(Context context) throws IOException {
        var songParams = Server.getJson().readTree(context.body());
        if (songParams == null || songParams.size() != 7 ||
                !songParams.hasNonNull("name") ||
                !songParams.hasNonNull("artist") ||
                !songParams.hasNonNull("album") ||
                !songParams.hasNonNull("boos") ||
                !songParams.hasNonNull("spotifySongId") ||
                !songParams.hasNonNull("albumArt") ||
                !songParams.hasNonNull("requestedBy") ||
                !songParams.get("name").isTextual() ||
                !songParams.get("artist").isTextual() ||
                !songParams.get("album").isTextual() ||
                !songParams.get("boos").isInt() ||
                !songParams.get("spotifySongId").isTextual() ||
                !songParams.get("albumArt").isTextual() ||
                !songParams.get("requestedBy").isArray())
            throw new BadRequestResponse("Malformed Song");

        var requestedByNodeList = songParams.get("requestedBy");
        var requestedByUUIDList = new ArrayList<UUID>();
        for (final JsonNode requestedBy : requestedByNodeList) {
            requestedByUUIDList.add(UUID.fromString(requestedBy.asText()));
        }

        return Song.builder()
                .name(songParams.get("name").asText())
                .artist(songParams.get("artist").asText())
                .album(songParams.get("album").asText())
                .boos(songParams.get("boos").asInt())
                .spotifySongId(songParams.get("spotifySongId").asText())
                .albumArt(songParams.get("albumArt").asText())
                .requestedBy(requestedByUUIDList)
                .build();
    }
}
