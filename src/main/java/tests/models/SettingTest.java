package tests.models;

import jhu.group6.sounDJam.models.Setting;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Setting.class })
public class SettingTest {
    private UUID settingId = UUID.randomUUID();
    private UUID sessionId = UUID.randomUUID();
    private int maxTimeLimit = 3;
    private int maxUserCanAdd = 2;
    private int minTimeLimit = 1;
    private double danceability = 0.1;
    private double energy = 0.2;
    private double valence = 0.3;
    private double tempo = 0.4;
    private int maxSongLength = 600;
    private int minSongLength = 120;

    private static List<String> blacklist = new ArrayList<>();
    private static List<String> preferredGenres = new ArrayList<>();


    @BeforeAll
    public static void init() {
        blacklist.add("one");
        preferredGenres.add("two");
    }

    @Test
    public void toDocument() {
        mockStatic(UUID.class);
        when(UUID.randomUUID()).thenReturn(settingId);

        var settingDocFull = Setting.builder()
            .settingId(settingId)
            .anarchyMode(true)
            .blacklist(blacklist)
            .maxSongLength(maxTimeLimit)
            .maxUserCanAdd(maxUserCanAdd)
            .minSongLength(minTimeLimit)
            .preferredGenres(preferredGenres)
            .explicitAllowed(false)
            .sessionId(sessionId)
            .danceability(danceability)
            .energy(energy)
            .valence(valence)
            .tempo(tempo)
            .build()
            .toDocument();

        assertEquals(settingDocFull.get("settingId"), settingId.toString());
        assertEquals(settingDocFull.get("anarchyMode"), true);
        assertEquals(settingDocFull.get("blacklist"), blacklist);
        assertEquals(settingDocFull.get("maxSongLength"), maxTimeLimit);
        assertEquals(settingDocFull.get("maxUserCanAdd"), maxUserCanAdd);
        assertEquals(settingDocFull.get("minSongLength"), minTimeLimit);
        assertEquals(settingDocFull.get("danceability"), danceability);
        assertEquals(settingDocFull.get("energy"), energy);
        assertEquals(settingDocFull.get("valence"), valence);
        assertEquals(settingDocFull.get("tempo"), tempo);
        assertTrue(Integer.parseInt(settingDocFull.get("minSongLength").toString())
                < Integer.parseInt(settingDocFull.get("maxSongLength").toString()));
        assertEquals(settingDocFull.get("preferredGenres"), preferredGenres);
        assertEquals(settingDocFull.get("explicitAllowed"), false);
        assertEquals(settingDocFull.get("sessionId"), sessionId.toString());

        when(UUID.randomUUID()).thenReturn(settingId);

        var settingDocEmpty = Setting.builder()
                .build()
                .toDocument();

        assertEquals(settingDocEmpty.get("settingId"), settingId.toString());
        assertEquals(settingDocEmpty.get("anarchyMode"), false);
        assertEquals(settingDocEmpty.get("blacklist"), new ArrayList<String>());
        assertEquals(settingDocEmpty.get("maxSongLength"), 600);
        assertEquals(settingDocEmpty.get("maxUserCanAdd"), 10);
        assertEquals(settingDocEmpty.get("minSongLength"), 120);
        assertEquals(settingDocEmpty.get("danceability"), 0.5);
        assertEquals(settingDocEmpty.get("energy"), 0.5);
        assertEquals(settingDocEmpty.get("valence"), 0.5);
        assertEquals(settingDocEmpty.get("tempo"), 120.0);
        assertTrue(Integer.parseInt(settingDocEmpty.get("minSongLength").toString())
                < Integer.parseInt(settingDocEmpty.get("maxSongLength").toString()));
        assertEquals(settingDocEmpty.get("preferredGenres"), new ArrayList<>());
        assertEquals(settingDocEmpty.get("explicitAllowed"), true);
        assertNull(settingDocEmpty.get("sessionId"));
    }

    @Test
    public void fromDocument() {
        mockStatic(UUID.class);
        when(UUID.randomUUID()).thenReturn(settingId);
        when(UUID.fromString(any(String.class)))
                .thenReturn(settingId)
                .thenReturn(sessionId);

        var settingDocFull = Setting.builder()
                .settingId(settingId)
                .anarchyMode(true)
                .blacklist(blacklist)
                .maxSongLength(maxTimeLimit)
                .maxUserCanAdd(maxUserCanAdd)
                .minSongLength(minTimeLimit)
                .preferredGenres(preferredGenres)
                .explicitAllowed(false)
                .sessionId(sessionId)
                .danceability(danceability)
                .energy(energy)
                .valence(valence)
                .tempo(tempo)
                .build()
                .toDocument();

        var settingFull = Setting.fromDocument(settingDocFull);

        assertEquals(settingFull.getSettingId(), settingId);
        assertTrue(settingFull.isAnarchyMode());
        assertEquals(settingFull.getBlacklist(), blacklist);
        assertEquals(settingFull.getMaxSongLength(), maxSongLength, 0);
        assertEquals(settingFull.getMaxUserCanAdd(), maxUserCanAdd);
        assertEquals(settingFull.getMinSongLength(), minSongLength, 0);
        assertEquals(settingFull.getDanceability(), danceability, 0);
        assertEquals(settingFull.getEnergy(), energy, 0);
        assertEquals(settingFull.getValence(), valence, 0);
        assertEquals(settingFull.getTempo(), tempo, 0);
        assertTrue(settingFull.getMinSongLength() < settingFull.getMaxSongLength());
        assertEquals(settingFull.getPreferredGenres(), preferredGenres);
        assertFalse(settingFull.isExplicitAllowed());
        assertEquals(settingFull.getSessionId(), sessionId);

        when(UUID.randomUUID()).thenReturn(settingId);
        when(UUID.fromString(any(String.class)))
                .thenReturn(settingId)
                .thenReturn(sessionId);

        var settingDocEmpty = Setting.builder()
                .build()
                .toDocument();

        var settingEmpty = Setting.fromDocument(settingDocEmpty);

        assertEquals(settingEmpty.getSettingId(), settingId);
        assertFalse(settingEmpty.isAnarchyMode());
        assertEquals(settingEmpty.getBlacklist(), new ArrayList<>());
        assertEquals(settingEmpty.getMaxSongLength(), 600);
        assertEquals(settingEmpty.getMaxUserCanAdd(), 10);
        assertEquals(settingEmpty.getMinSongLength(), 120);
        assertEquals(settingEmpty.getDanceability(), 0.5, 0);
        assertEquals(settingEmpty.getEnergy(), 0.5, 0);
        assertEquals(settingEmpty.getValence(), 0.5, 0);
        assertEquals(settingEmpty.getTempo(), 120.0, 0);
        assertTrue(settingEmpty.getMinSongLength() < settingEmpty.getMaxSongLength());
        assertEquals(settingEmpty.getPreferredGenres(), new ArrayList<>());
        assertTrue(settingEmpty.isExplicitAllowed());
        assertNull(settingEmpty.getSessionId());
    }

    @Test
    public void addToBlacklist() {
        var artist = "some artist";
        var setting = Setting.builder().build();
        assertEquals(new ArrayList<>(), setting.getBlacklist());
        setting.addToBlacklist(artist);
        assertEquals(Collections.singletonList(artist), setting.getBlacklist());
    }

    @Test
    public void setSettingId() {
    }

    @Test
    public void setMaxSongLength() {
    }

    @Test
    public void setMinSongLength() {
    }

    @Test
    public void setMaxUserCanAdd() {
    }

    @Test
    public void setMaxTempo() {
    }

    @Test
    public void setMinTempo() {
    }

    @Test
    public void setAnarchyMode() {
    }

    @Test
    public void setPreferredGenres() {
    }

    @Test
    public void setBlacklist() {
    }

    @Test
    public void setExplicitAllowed() {
    }

    @Test
    public void setDanceability() {
    }

    @Test
    public void setEnergy() {
    }

    @Test
    public void setValence() {
    }

    @Test
    public void setTempo() {
    }

    @Test
    public void setSessionId() {
    }
}