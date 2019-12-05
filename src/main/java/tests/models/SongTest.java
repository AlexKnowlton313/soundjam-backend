package tests.models;

import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import jhu.group6.sounDJam.controllers.SessionController;
import jhu.group6.sounDJam.controllers.UserController;
import jhu.group6.sounDJam.models.Session;
import jhu.group6.sounDJam.models.Setting;
import jhu.group6.sounDJam.models.Song;
import jhu.group6.sounDJam.models.User;
import org.junit.Test;

import java.time.Instant;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ UserController.class, SessionController.class })
public class SongTest {
    private String name = "debugging";
    private String artist = "yo mama";
    private String album = "generic christmas album";
    private String albumArt = "pic of danny devito";
    private String spotifySongId = "IDeeznutz";
    private double score = 2;
    private int boos = 11;
    private int duration = 260;
    private long timeAdded = 3L;
    private double danceability = 0.5;
    private double energy = 1.3;
    private double tempo = 80.2;
    private double valence = 1.4;
    private static List<UUID> requestedBy = Collections.singletonList(UUID.randomUUID());

    @Test
    public void testToDocument() {
        var songDocFull = Song.builder()
                .spotifySongId(spotifySongId)
                .album(album)
                .albumArt(albumArt)
                .artist(artist)
                .boos(boos)
                .name(name)
                .requestedBy(requestedBy)
                .duration(duration)
                .timeAdded(timeAdded)
                .danceability(danceability)
                .energy(energy)
                .tempo(tempo)
                .valence(valence)
                .score(score)
                .build()
                .toDocument();

        var requestedByStrings = (List<String>) songDocFull.get("requestedBy");
        var requestedByFromDoc = requestedByStrings.stream().map(UUID::fromString).collect(toList());

        assertEquals(songDocFull.get("spotifySongId"), spotifySongId);
        assertEquals(songDocFull.get("album"), album);
        assertEquals(songDocFull.get("albumArt"), albumArt);
        assertEquals(songDocFull.get("artist"), artist);
        assertEquals(songDocFull.get("boos"), boos);
        assertEquals(songDocFull.getLong("timeAdded"), timeAdded, 0);
        assertEquals(songDocFull.get("name"), name);
        assertEquals(requestedByFromDoc, requestedBy);
        assertEquals(songDocFull.get("duration"), duration);
        assertEquals(songDocFull.get("danceability"), danceability);
        assertEquals(songDocFull.get("energy"), energy);
        assertEquals(songDocFull.get("tempo"), tempo);
        assertEquals(songDocFull.get("valence"), valence);
        assertEquals(songDocFull.get("score"), score);

        var songDocEmpty = Song.builder()
                .build()
                .toDocument();

        assertNull(songDocEmpty.get("spotifySongId"));
        assertNull(songDocEmpty.get("album"));
        assertNull(songDocEmpty.get("albumArt"));
        assertNull(songDocEmpty.get("artist"));
        assertEquals(songDocEmpty.get("boos"), 0);
        assertEquals(songDocEmpty.getLong("timeAdded"), Instant.now().getEpochSecond(), 100);
        assertNull(songDocEmpty.get("name"));
        assertEquals(songDocEmpty.get("requestedBy"), new ArrayList<>());
        assertEquals(songDocEmpty.get("duration"), 0);
        assertEquals(songDocEmpty.get("danceability"), 0.0);
        assertEquals(songDocEmpty.get("energy"), 0.0);
        assertEquals(songDocEmpty.get("tempo"), 0.0);
        assertEquals(songDocEmpty.get("valence"), 0.0);
        assertEquals(songDocEmpty.get("score"), 0.0);
    }

    @Test
    public void testFromDocument() {
        var songDocFull = Song.builder()
                .spotifySongId(spotifySongId)
                .album(album)
                .albumArt(albumArt)
                .artist(artist)
                .boos(boos)
                .name(name)
                .timeAdded(timeAdded)
                .requestedBy(requestedBy)
                .duration(duration)
                .danceability(danceability)
                .energy(energy)
                .tempo(tempo)
                .valence(valence)
                .score(score)
                .build()
                .toDocument();

        var songFull = Song.fromDocument(songDocFull);

        assertEquals(songFull.getBoos(), boos);
        assertEquals(songFull.getRequestedBy(), requestedBy);
        assertEquals(songFull.getSpotifySongId(), spotifySongId);
        assertEquals(songFull.getAlbumArt(), albumArt);
        assertEquals(songFull.getAlbum(), album);
        assertEquals(songFull.getArtist(), artist);
        assertEquals(songFull.getTimeAdded(), timeAdded);
        assertEquals(songFull.getName(), name);
        assertEquals(songFull.getDuration(), duration);
        assertEquals(songFull.getDanceability(), danceability, 0);
        assertEquals(songFull.getEnergy(), energy, 0);
        assertEquals(songFull.getTempo(), tempo, 0);
        assertEquals(songFull.getValence(), valence, 0);
        assertEquals(songFull.getScore(), score, 0);

        var songDocEmpty = Song.builder()
                .build()
                .toDocument();

        var songEmpty = Song.fromDocument(songDocEmpty);

        assertEquals(songEmpty.getBoos(), 0);
        assertEquals(songEmpty.getRequestedBy(), new ArrayList<>());
        assertNull(songEmpty.getSpotifySongId());
        assertNull(songEmpty.getAlbumArt());
        assertNull(songEmpty.getAlbum());
        assertNull(songEmpty.getArtist());
        assertNull(songEmpty.getName());
        assertEquals(songEmpty.getTimeAdded(), Instant.now().getEpochSecond(), 100);
        assertEquals(songEmpty.getDuration(), 0);
        assertEquals(songEmpty.getDanceability(), 0.0, 0);
        assertEquals(songEmpty.getEnergy(), 0.0, 0);
        assertEquals(songEmpty.getTempo(), 0.0, 0);
        assertEquals(songEmpty.getValence(), 0.0, 0);
        assertEquals(songEmpty.getScore(), 0.0, 0);
    }

    @Test
    public void testSetAudioFeaturesNotNull() {
        var audioFeaturesMock = mock(AudioFeatures.class);
        when(audioFeaturesMock.getDurationMs()).thenReturn(duration);
        when(audioFeaturesMock.getDanceability()).thenReturn((float) danceability);
        when(audioFeaturesMock.getEnergy()).thenReturn((float) energy);
        when(audioFeaturesMock.getTempo()).thenReturn((float) tempo);
        when(audioFeaturesMock.getValence()).thenReturn((float) valence);

        var song = Song.builder().build();

        assertEquals(0, song.getDuration());
        assertEquals(0.0, song.getDanceability(), 0);
        assertEquals(0.0, song.getEnergy(), 0);
        assertEquals(0.0, song.getTempo(), 0);
        assertEquals(0.0, song.getValence(), 0);

        song.setAudioFeatures(audioFeaturesMock);

        assertEquals(duration, song.getDuration());
        assertEquals(danceability, song.getDanceability(), .01);
        assertEquals(energy, song.getEnergy(), .01);
        assertEquals(tempo, song.getTempo(), .01);
        assertEquals(valence, song.getValence(), .01);

        var inOrder = Mockito.inOrder(audioFeaturesMock);
        inOrder.verify(audioFeaturesMock).getDurationMs();
        inOrder.verify(audioFeaturesMock).getDanceability();
        inOrder.verify(audioFeaturesMock).getEnergy();
        inOrder.verify(audioFeaturesMock).getTempo();
        inOrder.verify(audioFeaturesMock).getValence();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testSetAudioFeaturesNull() {
        var song = Song.builder().build();

        assertEquals(0, song.getDuration());
        assertEquals(0.0, song.getDanceability(), 0);
        assertEquals(0.0, song.getEnergy(), 0);
        assertEquals(0.0, song.getTempo(), 0);
        assertEquals(0.0, song.getValence(), 0);

        song.setAudioFeatures(null);

        assertEquals(0, song.getDuration());
        assertEquals(0.0, song.getDanceability(), 0);
        assertEquals(0.0, song.getEnergy(), 0);
        assertEquals(0.0, song.getTempo(), 0);
        assertEquals(0.0, song.getValence(), 0);
    }

    @Test
    public void testBooOneUser() throws Exception {
        mockStatic(UserController.class);
        var user = User.builder().build();

        when(UserController.getUserFromId(anyString())).thenReturn(user);
        PowerMockito.doNothing().when(UserController.class, "incrementNumBoos", user);

        var song = Song.builder()
                .requestedBy(requestedBy)
                .build();

        assertEquals(0, song.getBoos());
        song.boo();
        assertEquals(1, song.getBoos());
    }

    @Test
    public void testBooNoUsers() throws Exception {
        mockStatic(UserController.class);
        var user = User.builder().build();
        when(UserController.getUserFromId(anyString())).thenReturn(user);

        var song = Song.builder().build();

        assertEquals(0, song.getBoos());
        song.boo();
        assertEquals(1, song.getBoos());
    }

    @Test
    public void testScoreBySettingWithFullSong() {
        var numBoos = 10;
        var sessionId = UUID.randomUUID();
        var partierIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        var numSongsAdded = 10;

        mockStatic(UserController.class);
        mockStatic(SessionController.class);

        var userMock = mock(User.class);
        var sessionMock = mock(Session.class);

        when(UserController.getUserFromId(anyString())).thenReturn(userMock);
        when(userMock.getNumBoos()).thenReturn(numBoos);
        when(userMock.getSessionId()).thenReturn(sessionId);
        when(SessionController.getSessionFromId(anyString())).thenReturn(sessionMock);
        when(sessionMock.getPartierIds()).thenReturn(partierIds);
        when(userMock.getNumSongsAdded()).thenReturn(numSongsAdded);

        var minTempo = 80.0;
        var maxTempo = 100.0;
        var maxSongLength = 300;
        var minSongLength = 200;

        var settingMock = mock(Setting.class);
        when(settingMock.getMinTempo()).thenReturn(minTempo);
        when(settingMock.getMaxTempo()).thenReturn(maxTempo);
        when(settingMock.getMaxSongLength()).thenReturn(maxSongLength);
        when(settingMock.getMinSongLength()).thenReturn(minSongLength);
        when(settingMock.getDanceability()).thenReturn(danceability);
        when(settingMock.getEnergy()).thenReturn(energy);
        when(settingMock.getValence()).thenReturn(valence);

        var song = Song.builder()
                .spotifySongId(spotifySongId)
                .album(album)
                .albumArt(albumArt)
                .artist(artist)
                .boos(boos)
                .name(name)
                .timeAdded(timeAdded)
                .requestedBy(requestedBy)
                .duration(duration)
                .danceability(danceability)
                .energy(energy)
                .tempo(tempo)
                .valence(valence)
                .score(score)
                .build();

        assertEquals(0.50, song.score(settingMock), .1);

        var inOrder = Mockito.inOrder(settingMock);
        inOrder.verify(settingMock).getMinTempo();
        inOrder.verify(settingMock).getMaxTempo();
        inOrder.verify(settingMock).getMinTempo();
        inOrder.verify(settingMock).getMaxSongLength();
        inOrder.verify(settingMock).getMinSongLength();
        inOrder.verify(settingMock).getDanceability();
        inOrder.verify(settingMock).getEnergy();
        inOrder.verify(settingMock).getValence();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testScoreBySettingWithEmptySong() {
        var minTempo = 80.0;
        var maxTempo = 100.0;
        var maxSongLength = 300;
        var minSongLength = 200;

        var settingMock = mock(Setting.class);
        when(settingMock.getMinTempo()).thenReturn(minTempo);
        when(settingMock.getMaxTempo()).thenReturn(maxTempo);
        when(settingMock.getMaxSongLength()).thenReturn(maxSongLength);
        when(settingMock.getMinSongLength()).thenReturn(minSongLength);
        when(settingMock.getDanceability()).thenReturn(danceability);
        when(settingMock.getEnergy()).thenReturn(energy);
        when(settingMock.getValence()).thenReturn(valence);

        var song = Song.builder().build();

        assertEquals(0.20, song.score(settingMock), .1);

        var inOrder = Mockito.inOrder(settingMock);
        inOrder.verify(settingMock).getMinTempo();
        inOrder.verify(settingMock).getMaxTempo();
        inOrder.verify(settingMock).getMinTempo();
        inOrder.verify(settingMock).getMaxSongLength();
        inOrder.verify(settingMock).getMinSongLength();
        inOrder.verify(settingMock).getDanceability();
        inOrder.verify(settingMock).getEnergy();
        inOrder.verify(settingMock).getValence();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testScoreBySongWithFullSongs() {
        var songParam = Song.builder()
                .spotifySongId(spotifySongId)
                .album(album)
                .albumArt(albumArt)
                .artist(artist)
                .boos(boos)
                .name(name)
                .timeAdded(timeAdded)
                .requestedBy(requestedBy)
                .duration(duration)
                .danceability(danceability)
                .energy(energy)
                .tempo(tempo)
                .valence(valence)
                .score(score)
                .build();

        var song = Song.builder()
                .spotifySongId(spotifySongId)
                .album(album)
                .albumArt(albumArt)
                .artist(artist)
                .boos(boos)
                .name(name)
                .timeAdded(timeAdded)
                .requestedBy(requestedBy)
                .duration(duration)
                .danceability(danceability)
                .energy(energy)
                .tempo(tempo)
                .valence(valence)
                .score(score)
                .build();

        var score = song.score(songParam);

        assertEquals(0, score, 0);
    }


    @Test
    public void testScoreBySongWithFullSongsWhereBaseSongIsGreater() {
        var songParam = Song.builder()
                .spotifySongId(spotifySongId)
                .album(album)
                .albumArt(albumArt)
                .artist(artist)
                .boos(boos)
                .name(name)
                .timeAdded(timeAdded)
                .requestedBy(requestedBy)
                .duration(duration)
                .danceability(danceability)
                .energy(energy)
                .tempo(tempo)
                .valence(valence)
                .score(score)
                .build();

        var song = Song.builder()
                .spotifySongId(spotifySongId)
                .album(album)
                .albumArt(albumArt)
                .artist(artist)
                .boos(boos)
                .name(name)
                .timeAdded(timeAdded)
                .requestedBy(requestedBy)
                .duration(duration + 1)
                .danceability(danceability + 1)
                .energy(energy + 1)
                .tempo(tempo)
                .valence(valence + 1)
                .score(score)
                .build();

        var score = song.score(songParam);

        assertEquals(0, score, 0.01);
    }

    @Test
    public void testScoreBySongWithEmptySongs() {
        var songParam = Song.builder().build();
        var song = Song.builder().build();

        var score = song.score(songParam);

        assertEquals(0, score, 0);
    }

    @Test
    public void booScoreMoreThanOnePartierHelper() {
        var numBoos = 10;
        var sessionId = UUID.randomUUID();
        var partierIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        var numSongsAdded = 10;

        mockStatic(UserController.class);
        mockStatic(SessionController.class);

        var userMock = mock(User.class);
        var sessionMock = mock(Session.class);

        when(UserController.getUserFromId(anyString())).thenReturn(userMock);
        when(userMock.getNumBoos()).thenReturn(numBoos);
        when(userMock.getSessionId()).thenReturn(sessionId);
        when(SessionController.getSessionFromId(anyString())).thenReturn(sessionMock);
        when(sessionMock.getPartierIds()).thenReturn(partierIds);
        when(userMock.getNumSongsAdded()).thenReturn(numSongsAdded);

        var song = Song.builder()
                .requestedBy(partierIds)
                .build();

        var score = song.booScore();

        // it should be 10 / ((2 + 1) * 10) = 1/3
        assertEquals(.333, score, .01);

        var inOrder = Mockito.inOrder(userMock, sessionMock);
        inOrder.verify(userMock, times(3)).getNumBoos();
        inOrder.verify(userMock).getSessionId();
        inOrder.verify(sessionMock).getPartierIds();
        inOrder.verify(userMock).getNumSongsAdded();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void booScoreZeroPartiers() {
        var song = Song.builder().build();
        var score = song.booScore();
        assertEquals(0, score, 0);
    }

    @Test
    public void booScoreMoreNullPartiers() {
        var song = Song.builder().requestedBy(null).build();
        var score = song.booScore();
        assertEquals(0, score, 0);
    }

    @Test
    public void durationScoreInBetweenMaxAndMin() {
        var maxSongLength = 300;
        var minSongLength = 200;

        var settingMock = mock(Setting.class);
        when(settingMock.getMaxSongLength()).thenReturn(maxSongLength);
        when(settingMock.getMinSongLength()).thenReturn(minSongLength);

        var song = Song.builder()
                .duration(duration)
                .build();

        assertEquals(0.8, song.durationScore(settingMock), 0);

        var inOrder = Mockito.inOrder(settingMock);
        inOrder.verify(settingMock).getMaxSongLength();
        inOrder.verify(settingMock).getMinSongLength();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void durationScoreInBetweenGreaterThanMax() {
        var maxSongLength = 200;
        var minSongLength = 100;

        var settingMock = mock(Setting.class);
        when(settingMock.getMaxSongLength()).thenReturn(maxSongLength);
        when(settingMock.getMinSongLength()).thenReturn(minSongLength);

        var song = Song.builder()
                .duration(duration)
                .build();

        assertEquals(0, song.durationScore(settingMock), 0);

        var inOrder = Mockito.inOrder(settingMock);
        inOrder.verify(settingMock).getMaxSongLength();
        inOrder.verify(settingMock).getMinSongLength();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void durationScoreInBetweenLessThanMin() {
        var maxSongLength = 400;
        var minSongLength = 300;

        var settingMock = mock(Setting.class);
        when(settingMock.getMaxSongLength()).thenReturn(maxSongLength);
        when(settingMock.getMinSongLength()).thenReturn(minSongLength);

        var song = Song.builder()
                .duration(duration)
                .build();

        assertEquals(0, song.durationScore(settingMock), 0);

        var inOrder = Mockito.inOrder(settingMock);
        inOrder.verify(settingMock).getMaxSongLength();
        inOrder.verify(settingMock).getMinSongLength();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testGetSetName() {
        var song = Song.builder().build();
        assertNull(song.getName());
        song.setName(name);
        assertEquals(name, song.getName());
    }

    @Test
    public void testGetSetArtist() {
        var song = Song.builder().build();
        assertNull(song.getArtist());
        song.setArtist(artist);
        assertEquals(artist, song.getArtist());
    }

    @Test
    public void testGetSetAlbum() {
        var song = Song.builder().build();
        assertNull(song.getAlbum());
        song.setAlbum(album);
        assertEquals(album, song.getAlbum());
    }

    @Test
    public void testGetSetBoos() {
        var song = Song.builder().build();
        assertEquals(0, song.getBoos());
        song.setBoos(boos);
        assertEquals(boos, song.getBoos());
    }

    @Test
    public void testGetSetSpotifySongId() {
        var song = Song.builder().build();
        assertNull(song.getSpotifySongId());
        song.setSpotifySongId(spotifySongId);
        assertEquals(spotifySongId, song.getSpotifySongId());
    }

    @Test
    public void testGetSetAlbumArt() {
        var song = Song.builder().build();
        assertNull(song.getAlbumArt());
        song.setAlbumArt(albumArt);
        assertEquals(albumArt, song.getAlbumArt());
    }

    @Test
    public void testGetSetRequestedBy() {
        var song = Song.builder().build();
        assertEquals(new ArrayList<UUID>(), song.getRequestedBy());
        song.setRequestedBy(requestedBy);
        assertArrayEquals(new List[]{requestedBy}, new List[]{song.getRequestedBy()});
    }

    @Test
    public void testGetSetTimeScore() {
        var timeScore = 10.0;

        var song = Song.builder().build();
        assertEquals(0, song.getTimeScore(), 0);
        song.setTimeScore(timeScore);
        assertEquals(timeScore, song.getTimeScore(), 0);
    }

    @Test
    public void testGetSetScore() {
        var song = Song.builder().build();
        assertEquals(0, song.getScore(), 0);
        song.setScore(score);
        assertEquals(score, song.getScore(), 0);
    }

    @Test
    public void testGetSetDuration() {
        var song = Song.builder().build();
        assertEquals(0, song.getDuration());
        song.setDuration(duration);
        assertEquals(duration, song.getDuration());
    }

    @Test
    public void testGetSetDanceability() {
        var song = Song.builder().build();
        assertEquals(0, song.getDanceability(), 0);
        song.setDanceability(danceability);
        assertEquals(danceability, song.getDanceability(), 0);
    }

    @Test
    public void testGetSetEnergy() {
        var song = Song.builder().build();
        assertEquals(0, song.getEnergy(), 0);
        song.setEnergy(energy);
        assertEquals(energy, song.getEnergy(), 0);
    }

    @Test
    public void testGetSetTempo() {
        var song = Song.builder().build();
        assertEquals(0, song.getTempo(), 0);
        song.setTempo(tempo);
        assertEquals(tempo, song.getTempo(), 0);
    }

    @Test
    public void testGetSetValence() {
        var song = Song.builder().build();
        assertEquals(0, song.getValence(), 0);
        song.setValence(valence);
        assertEquals(valence, song.getValence(), 0);
    }
}