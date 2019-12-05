package jhu.group6.sounDJam.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Builder
@Getter
@Setter
public class Session {
    @Builder.Default private UUID sessionId = UUID.randomUUID();
    private String name;
    private UUID djId;
    @Builder.Default private List<UUID> partierIds = new ArrayList<>();
    private Song currentSong;
    private UUID settingId;
    private UUID queueId;
    private String accessToken;
    private String refreshToken;
    @Builder.Default private UUID state = UUID.randomUUID();
    private long lastUpdated = System.currentTimeMillis();

    public Document toDocument() {
        var doc = new Document("name", this.name)
                .append("accessToken", this.accessToken)
                .append("refreshToken", this.refreshToken);
        if (this.djId != null) doc.append("djId", this.djId.toString());
        if (this.sessionId != null) doc.append("sessionId", this.sessionId.toString());
        if (this.queueId != null) doc.append("queueId", this.queueId.toString());
        if (this.settingId != null) doc.append("settingId", this.settingId.toString());
        if (this.partierIds != null) doc.append("partierIds", this.partierIds.stream().map(UUID::toString).collect(toList()));
        if (this.currentSong != null) doc.append("currentSong", this.currentSong.toDocument());
        if (this.state != null) doc.append("state", this.state.toString());
        doc.append("lastUpdated", this.lastUpdated);

        return doc;
    }

    public static Session fromDocument(Document doc) {
        var sessionId = doc.get("sessionId") == null ? null : UUID.fromString(doc.getString("sessionId"));
        var state = doc.get("state") == null ? null : UUID.fromString(doc.getString("state"));
        var name = doc.get("name") == null ? null : doc.getString("name");
        var djId = doc.get("djId") == null ? null : UUID.fromString(doc.getString("djId"));
        var partierIdDocs = doc.get("partierIds") == null ? null : (List<String>) doc.get("partierIds");
        var currentSongDoc = doc.get("currentSong") == null ? null : (Document) doc.get("currentSong");
        var settingId = doc.get("settingId") == null ? null : UUID.fromString(doc.getString("settingId"));
        var queueId = doc.get("queueId") == null ? null : UUID.fromString(doc.getString("queueId"));
        var accessToken = doc.get("accessToken") == null ? null : doc.getString("accessToken");
        var refreshToken = doc.get("refreshToken") == null ? null : doc.getString("refreshToken");
        var lastUpdated = doc.get("lastUpdated") == null ? (long) 0 : doc.getLong("lastUpdated");

        List<UUID> partierIds = partierIdDocs == null ? new ArrayList<>() : partierIdDocs.stream().map(UUID::fromString).collect(toList());
        var currentSong = currentSongDoc == null ? null : Song.fromDocument(currentSongDoc);

        return Session.builder()
                .sessionId(sessionId)
                .name(name)
                .djId(djId)
                .partierIds(partierIds)
                .currentSong(currentSong)
                .settingId(settingId)
                .queueId(queueId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .state(state)
                .lastUpdated(lastUpdated)
                .build();
    }
}
