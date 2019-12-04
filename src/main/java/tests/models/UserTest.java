package tests.models;

import jhu.group6.sounDJam.models.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ User.class })
public class UserTest {
    private UUID userId = UUID.randomUUID();
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
        mockStatic(UUID.class);
        when(UUID.randomUUID()).thenReturn(userId);

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
        assertEquals(userFull.get("userId"), userId.toString());

        when(UUID.randomUUID()).thenReturn(userId);

        var userEmpty = User.builder()
                .build()
                .toDocument();

        assertNull(userEmpty.get("nickname"));
        assertNull(userEmpty.get("sessionId"));
        assertEquals(userEmpty.get("numSongsAdded"), 0);
        assertEquals(userEmpty.get("numBoos"), 0);
        assertEquals(userEmpty.get("userId"), userId.toString());
    }

    @Test
    public void testFromDocument() {
        mockStatic(UUID.class);
        when(UUID.randomUUID()).thenReturn(userId);
        when(UUID.fromString(any(String.class))).thenReturn(userId).thenReturn(sessionId);

        var userFull = User.builder()
                .nickname(nickname)
                .sessionId(sessionId)
                .numSongsAdded(numSongsAdded)
                .numBoos(numBoos)
                .build()
                .toDocument();

        var userDocFull = User.fromDocument(userFull);

        assertEquals(userDocFull.getUserId(), userId);
        assertEquals(userDocFull.getNumSongsAdded(), numSongsAdded);
        assertEquals(userDocFull.getNumBoos(), numBoos);
        assertEquals(userDocFull.getSessionId(), sessionId);
        assertEquals(userDocFull.getNickname(), nickname);

        when(UUID.randomUUID()).thenReturn(userId);
        when(UUID.fromString(any(String.class))).thenReturn(userId);

        var userEmpty = User.builder()
                .build()
                .toDocument();

        var userDocEmpty = User.fromDocument(userEmpty);

        assertEquals(userDocEmpty.getUserId(), userId);
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
        mockStatic(UUID.class);
        when(UUID.randomUUID()).thenReturn(userId);

        var user = User.builder().build();
        assertEquals(userId, user.getUserId());
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