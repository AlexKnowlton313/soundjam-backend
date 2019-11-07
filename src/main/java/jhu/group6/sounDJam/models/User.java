package jhu.group6.sounDJam.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

@Builder
@Getter
@Setter
public class User {
    private String nickname;
    private UUID sessionId;
    @Builder.Default private UUID userId = UUID.randomUUID();
    @Builder.Default private int numSongsAdded = 0;
    @Builder.Default private int numBoos = 0;

    public void incrementNumSongsAdded() {
        this.numSongsAdded++;
    }

    public void incrementNumBoos() {
        this.numBoos++;
    }

    public Document toDocument() {
        var doc = new Document("nickname", this.nickname)
                .append("numSongsAdded", this.numSongsAdded)
                .append("numBoos", this.numBoos);
        if (this.userId != null) doc.append("userId", this.userId.toString());
        if (this.sessionId != null) doc.append("sessionId", this.sessionId.toString());

        return doc;
    }

    public static User fromDocument(Document doc) {
        var userId = doc.get("userId") == null ? null : UUID.fromString(doc.getString("userId"));
        var sessionId = doc.get("sessionId") == null ? null : UUID.fromString(doc.getString("sessionId"));
        var nickname = doc.get("nickname") == null ? null : doc.getString("nickname");
        var numSongsAdded = doc.get("numSongsAdded") == null ? 0 : doc.getInteger("numSongsAdded");
        var numBoos = doc.get("numBoos") == null ? 0 : doc.getInteger("numBoos");

        return User.builder()
                .sessionId(sessionId)
                .nickname(nickname)
                .userId(userId)
                .numSongsAdded(numSongsAdded)
                .numBoos(numBoos)
                .build();
    }

}
