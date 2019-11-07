package jhu.group6.sounDJam.exceptions;

import io.javalin.HttpResponseException;
import jhu.group6.sounDJam.models.Setting;
import jhu.group6.sounDJam.models.Song;
import jhu.group6.sounDJam.models.User;

import java.util.HashMap;
import java.util.UUID;

public class SongBlacklistedException extends HttpResponseException {
    public SongBlacklistedException(Song song, Setting setting) {
        this(song.getSpotifySongId(), setting);
    }

    public SongBlacklistedException(String songId, Setting setting) {
        super(401, "The artist you requested has been blacklisted by the DJ!", new HashMap<>());
    }
}
