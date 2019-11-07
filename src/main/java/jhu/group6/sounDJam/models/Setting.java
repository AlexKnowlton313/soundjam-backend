package jhu.group6.sounDJam.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

@Builder
@Getter
@Setter
public class Setting {
    @Builder.Default private UUID settingId = UUID.randomUUID();
    @Builder.Default private int maxSongLength = 600;
    @Builder.Default private int minSongLength = 120;
    @Builder.Default private int maxUserCanAdd = 10; // Should this be changed to maxUserCanAdd per X minutes?
    @Builder.Default private double maxTempo = 240.0; //needed for scoring calculation
    @Builder.Default private double minTempo = 40.0; //needed for scoring calculation
    @Builder.Default private boolean anarchyMode = false;
    @Builder.Default private List<String> preferredGenres = new ArrayList<>();
    @Builder.Default private List<String> blacklist = new ArrayList<>();
    @Builder.Default private boolean explicitAllowed = true;
    @Builder.Default private double danceability = 0.5;
    @Builder.Default private double energy = 0.5;
    @Builder.Default private double valence = 0.5;
    @Builder.Default private double tempo = 120.0;
    private UUID sessionId;



    public Document toDocument() {
        var doc =  new Document("maxSongLength", this.maxSongLength)
                .append("minSongLength", this.minSongLength)
                .append("maxUserCanAdd", this.maxUserCanAdd)
                .append("anarchyMode", this.anarchyMode)
                .append("preferredGenres", this.preferredGenres)
                .append("blacklist", this.blacklist)
                .append("explicitAllowed", this.explicitAllowed)
                .append("danceability", this.danceability)
                .append("energy", this.energy)
                .append("valence", this.valence)
                .append("tempo", this.tempo)
                .append("maxTempo", this.maxTempo)
                .append("minTempo", this.minTempo);
        if (this.settingId != null) doc.append("settingId", this.settingId.toString());
        if (this.sessionId != null) doc.append("sessionId", this.sessionId.toString());

        return doc;
    }

    public static Setting fromDocument(Document doc) {
        var settingId = doc.get("settingId") == null ? null : UUID.fromString(doc.getString("settingId"));
        var sessionId = doc.get("sessionId") == null ? null : UUID.fromString(doc.getString("sessionId"));
        var maxSongLength = doc.get("maxTimeLimit") == null ? 600 : doc.getInteger("maxTimeLimit");
        var minSongLength = doc.get("minTimeLimit") == null ? 120 : doc.getInteger("minTimeLimit");
        var maxUserCanAdd = doc.get("maxUserCanAdd") == null ? 10 : doc.getInteger("maxUserCanAdd");
        var anarchyMode = doc.get("anarchyMode") == null ? false : doc.getBoolean("anarchyMode");
        var explicitAllowed = doc.get("explicitAllowed") == null ? true : doc.getBoolean("explicitAllowed");
        var preferredGenres = doc.get("preferredGenres") == null ? null : (List<String>) doc.get("preferredGenres");
        var blacklist = doc.get("blacklist") == null ? null : (List<String>) doc.get("blacklist");
        var danceability = doc.get("danceability") == null ? 0.5 : doc.getDouble("danceability");
        var energy = doc.get("energy") == null ? 0.5 : doc.getDouble("energy");
        var tempo = doc.get("tempo") == null ? 120.0 : doc.getDouble("tempo");
        var valence = doc.get("valence") == null ? 0.5 : doc.getDouble("valence");
        var maxTempo = doc.get("maxTempo") == null ? 240.0 : doc.getDouble("maxTempo");
        var minTempo = doc.get("minTempo") == null ? 40.0 : doc.getDouble("minTempo");

        return Setting.builder()
                .settingId(settingId)
                .sessionId(sessionId)
                .maxSongLength(maxSongLength)
                .minSongLength(minSongLength)
                .maxUserCanAdd(maxUserCanAdd)
                .anarchyMode(anarchyMode)
                .explicitAllowed(explicitAllowed)
                .preferredGenres(preferredGenres)
                .blacklist(blacklist)
                .danceability(danceability)
                .energy(energy)
                .tempo(tempo)
                .valence(valence)
                .maxTempo(maxTempo)
                .minTempo(minTempo)
                .build();
    }

    public void addToBlacklist(String artist) {
        blacklist.add(artist);
    }
}