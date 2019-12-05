package tests.models;

import jhu.group6.sounDJam.models.User;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class UserTest {
    private UUID sessionId = UUID.randomUUID();
    private String nickname = "debugging";
    private int numSongsAdded = 20;
    private int numBoos = 11;

    @Test
    public void testIncrementNumSongsAdded() {
        var user = User.builder().build();

        assertEquals(user.getNumSongsAdded(), 0);
        user.incrementNumSongsAdded();
        assertEquals(1, user.getNumSongsAdded());
    }

    @Test
    public void testIncrementNumBoos() {
        var user = User.builder().build();

        assertEquals(user.getNumBoos(), 0);
        user.incrementNumBoos();
        assertEquals(1, user.getNumBoos());
    }

    @Test
    public void testToDocument() {
        var userFull = User.builder()
                .nickname(nickname)
                .sessionId(sessionId)
                .numSongsAdded(numSongsAdded)
                .numBoos(numBoos)
                .build()
                .toDocument();

        assertEquals(userFull.get("nickname"), nickname);
        assertEquals(userFull.get("sessionId"), sessionId.toString());
        assertEquals(userFull.get("numSongsAdded"), numSongsAdded);
        assertEquals(userFull.get("numBoos"), numBoos);

        try {
            UUID.fromString((String) userFull.get("userId"));
        } catch (Exception e) {
            fail();
        }

        var userEmpty = User.builder()
                .build()
                .toDocument();

        assertNull(userEmpty.get("nickname"));
        assertNull(userEmpty.get("sessionId"));
        assertEquals(userEmpty.get("numSongsAdded"), 0);
        assertEquals(userEmpty.get("numBoos"), 0);

        try {
            UUID.fromString((String) userEmpty.get("userId"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testFromDocument() {
        var userFull = User.builder()
                .nickname(nickname)
                .sessionId(sessionId)
                .numSongsAdded(numSongsAdded)
                .numBoos(numBoos)
                .build()
                .toDocument();

        var userDocFull = User.fromDocument(userFull);

        assertEquals(userDocFull.getNumSongsAdded(), numSongsAdded);
        assertEquals(userDocFull.getNumBoos(), numBoos);
        assertEquals(userDocFull.getSessionId(), sessionId);
        assertEquals(userDocFull.getNickname(), nickname);

        if (userDocFull.getUserId() == null) {
            fail();
        }

        var userEmpty = User.builder()
                .build()
                .toDocument();

        var userDocEmpty = User.fromDocument(userEmpty);

        if (userDocEmpty.getUserId() == null) {
            fail();
        }

        assertEquals(userDocEmpty.getNumSongsAdded(), 0);
        assertEquals(userDocEmpty.getNumBoos(), 0);
        assertNull(userDocEmpty.getSessionId());
        assertNull(userDocEmpty.getNickname());
    }

    @Test
    public void testGetSetNickname() {
        var user = User.builder().build();
        assertNull(user.getNickname());
        user.setNickname(nickname);
        assertEquals(nickname, user.getNickname());
    }

    @Test
    public void testGetSetSessionId() {
        var user = User.builder().build();
        assertNull(user.getSessionId());
        user.setSessionId(sessionId);
        assertEquals(sessionId, user.getSessionId());
    }

    @Test
    public void testGetSetUserId() {
        var user = User.builder().build();

        if (user.getUserId() == null) {
            fail();
        }

        user.setUserId(sessionId);
        assertEquals(sessionId, user.getUserId());
    }

    @Test
    public void testGetSetNumSongsAdded() {
        var user = User.builder().build();
        assertEquals(0, user.getNumSongsAdded());
        user.setNumSongsAdded(numSongsAdded);
        assertEquals(numSongsAdded, user.getNumSongsAdded());
    }

    @Test
    public void testGetSetNumBoos() {
        var user = User.builder().build();
        assertEquals(0, user.getNumBoos());
        user.setNumBoos(numBoos);
        assertEquals(numBoos, user.getNumBoos());
    }
}