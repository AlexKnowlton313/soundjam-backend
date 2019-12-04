package jhu.group6.sounDJam.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static jhu.group6.sounDJam.controllers.UserController.incrementNumSongsAdded;

@Builder
@Getter
@Setter
public class Queue {
    @Builder.Default private UUID queueId = UUID.randomUUID();
    @Builder.Default private double score = 0.;
    @Builder.Default private List<String> playedSongIds = new ArrayList<>();
    @Builder.Default private List<Song> songs = new ArrayList<>();
    @Builder.Default private long minTime = -1;
    @Builder.Default private long maxTime = -1;
    private UUID sessionId;

    public void recommendSong(Song recommendedSong, User user) {
        var foundSongs = this.songs.stream().filter(s -> s.getSpotifySongId().equals(recommendedSong.getSpotifySongId())).collect(toList());
        var song = recommendedSong;

        if (foundSongs.size() > 0) {
            song = foundSongs.get(0);
            this.songs.remove(song);
        }

        song.getRequestedBy().add(user.getUserId());
        this.songs.add(song);
        incrementNumSongsAdded(user);
    }

    public void adjustTimeScore() {
        this.setMinTime(this.getSongs().stream()
                .min(Comparator.comparing(Song::getTimeAdded))
                .get().getTimeAdded());

        this.setMaxTime(this.getSongs().stream()
                .max(Comparator.comparing(Song::getTimeAdded))
                .get().getTimeAdded());

        this.getSongs().stream()
                .forEach(song -> {song.setTimeScore(
                        1.0 - (double) (song.getTimeAdded() - this.getMinTime()) / (double) (this.getMaxTime() - this.getMinTime())
                );});
    }

    public void addPlayedSongId(String songId) {
        this.playedSongIds.add(songId);
    }

    public String getPlayedSongIdsAsString(){
        if (this.playedSongIds.size() == 0) return "";

        var songIds = new StringBuilder();
        if (this.playedSongIds.size() <= 5) {
            for (var songId : this.playedSongIds) {
                songIds.append(songId).append(",");
            }
        } else {
            for (var songId : this.playedSongIds.subList(this.playedSongIds.size()-5, this.playedSongIds.size())) {
                songIds.append(songId).append(",");
            }
        }
        songIds.deleteCharAt(songIds.length() - 1);
        return songIds.toString();
    }

    public Song popNextSong() {
        if (songs.size() == 0) {
            return null;
        }
        var song = songs.remove(0);

        playedSongIds.add(song.getSpotifySongId());
        return song;
    }

    public Document toDocument() {
        var doc = new Document("score", this.score);
        doc.append("minTime", this.minTime);
        doc.append("maxTime", this.maxTime);
        if (this.queueId != null) doc.append("queueId",  this.queueId.toString());
        if (this.sessionId != null) doc.append("sessionId", this.sessionId.toString());
        if (this.playedSongIds != null) doc.append("playedSongIds", this.playedSongIds);
        if (this.songs != null) doc.append("songs", this.songs.stream().map(Song::toDocument).collect(toList()));

        return doc;
    }

    public static Queue fromDocument(Document doc) {
        var queueId = doc.get("queueId") == null ? null : UUID.fromString(doc.getString("queueId"));
        var sessionId = doc.get("sessionId") == null ? null : UUID.fromString(doc.getString("sessionId"));
        var score = doc.get("score") == null ? 0. : doc.getDouble("score");
        var playedSongIds = doc.get("playedSongIds") == null ? new ArrayList<String>() : (List<String>) doc.get("playedSongIds", List.class);
        var songsDocs = doc.get("songs") == null ? new ArrayList<Document>() : (List<Document>) doc.get("songs", List.class);
        var songs = songsDocs == null ? null : songsDocs.stream().map(Song::fromDocument).collect(toList());
        var minTime = doc.get("minTime") == null ? (long) -1 : doc.getLong("minTime");
        var maxTime = doc.get("maxTime") == null ? (long) -1 : doc.getLong("maxTime");

        return Queue.builder()
                .queueId(queueId)
                .sessionId(sessionId)
                .score(score)
                .playedSongIds(playedSongIds)
                .songs(songs)
                .minTime(minTime)
                .maxTime(maxTime)
                .build();
    }

    public long getNumSongsAddedPastHourForUser(User user) {
        return songs.stream()
                .filter(song -> song.getTimeAdded() > (Instant.now().getEpochSecond() - 3600))
                .filter(song -> song.getRequestedBy().stream()
                        .filter(uuid -> uuid.toString().equals(user.getUserId().toString()))
                        .count() >= 1)
                .count();
    }
}
