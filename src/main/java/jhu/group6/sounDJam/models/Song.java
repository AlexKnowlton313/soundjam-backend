package jhu.group6.sounDJam.models;

import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import jhu.group6.sounDJam.controllers.SessionController;
import jhu.group6.sounDJam.controllers.UserController;
import jhu.group6.sounDJam.utils.MathUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.time.Instant;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Builder
@Getter
@Setter
public class Song {
    private String name;
    private String artist;
    private String album;
    @Builder.Default private int boos = 0;
    private String spotifySongId;
    private String albumArt;
    @Builder.Default private List<UUID> requestedBy = new ArrayList<>();
    @Builder.Default final private long timeAdded = Instant.now().getEpochSecond();
    @Builder.Default private double timeScore = 0.0;
    @Builder.Default private double score = 0.0;
    private int duration;
    private double danceability;
    private double energy;
    private double tempo;
    private double valence;
    private static double epsilon = .0001;

    public Document toDocument() {
        var doc = new Document("name", this.name)
                .append("artist", this.artist)
                .append("album", this.album)
                .append("boos", this.boos)
                .append("spotifySongId", this.spotifySongId)
                .append("albumArt", this.albumArt)
                .append("timeAdded", this.timeAdded)
                .append("duration", this.duration)
                .append("danceability", this.danceability)
                .append("energy", this.energy)
                .append("tempo", this.tempo)
                .append("valence", this.valence)
                .append("score", this.score)
                .append("timeScore", this.timeScore);
         if (this.requestedBy != null) doc.append("requestedBy", this.requestedBy.stream().map(UUID::toString).collect(toList()));

         return doc;
    }

    public static Song fromDocument(Document doc) {
        var name = doc.get("name") == null ? null : doc.getString("name");
        var artist = doc.get("artist") == null ? null : doc.getString("artist");
        var album = doc.get("album") == null ? null : doc.getString("album");
        var boos = doc.get("boos") == null ? 0 : doc.getInteger("boos");
        var spotifySongId = doc.get("spotifySongId") == null ? null : doc.getString("spotifySongId");
        var albumArt = doc.get("albumArt") == null ? null : doc.getString("albumArt");
        var timeAdded = doc.get("timeAdded") == null ? 0L : doc.getLong("timeAdded");
        var requestedByStrings = doc.get("requestedBy") == null ? null : (List<String>) doc.get("requestedBy");
        var duration = doc.get("duration") == null ? 0 : doc.getInteger("duration");
        var danceability = doc.get("danceability") == null ? 0.0 : doc.getDouble("danceability");
        var energy = doc.get("energy") == null ? 0.0 : doc.getDouble("energy");
        var tempo = doc.get("tempo") == null ? 0.0 : doc.getDouble("tempo");
        var valence = doc.get("valence") == null ? 0.0 : doc.getDouble("valence");
        var score = doc.get("score") == null ? 0.0 : doc.getDouble("score");
        var timeScore = doc.get("timeScore") == null ? 0.0 : doc.getDouble("timeScore");

        List<UUID> requestedBy = requestedByStrings == null ? new ArrayList<>() : requestedByStrings.stream().map(UUID::fromString).collect(toList());

        return Song.builder()
                .name(name)
                .artist(artist)
                .album(album)
                .boos(boos)
                .spotifySongId(spotifySongId)
                .albumArt(albumArt)
                .requestedBy(requestedBy)
                .duration(duration)
                .timeAdded(timeAdded)
                .danceability(danceability)
                .energy(energy)
                .tempo(tempo)
                .valence(valence)
                .score(score)
                .timeScore(timeScore)
                .build();
    }

    public void setAudioFeatures(AudioFeatures audioFeatures) {
        if (audioFeatures == null) {
            return;
        }
        this.duration = audioFeatures.getDurationMs();
        this.danceability = audioFeatures.getDanceability();
        this.energy = audioFeatures.getEnergy();
        this.tempo = audioFeatures.getTempo();
        this.valence = audioFeatures.getValence();
    }

    public void boo() {
        this.boos++;
        for (int i = 0; i < this.requestedBy.size(); i++) {
            User user = UserController.getUserFromId(this.requestedBy.get(i).toString());
            UserController.incrementNumBoos(user);
        }
    }


    public double score(Setting setting) {
        //song attributes
        double[] attributes = new double[7];
        double attributeCoefficient = 5.0;
        double booCoefficient = 0.1;
        double timeCoefficient = 0.1;

        //values between 0 and 1
        attributes[0] = attributeCoefficient * this.getDanceability();
        attributes[1] = attributeCoefficient * this.getEnergy();
        attributes[2] = attributeCoefficient * this.getValence();

        //normalized between 0 and 1
        double tempo = (this.getTempo() - setting.getMinTempo()) / (setting.getMaxTempo() - setting.getMinTempo());
        attributes[3] = tempo;
        attributes[4] = this.durationScore(setting);
        attributes[5] = timeCoefficient * this.getTimeScore();
        attributes[6] = booCoefficient * this.booScore();


        //setting attributes
        double[] settings = new double[7];

        settings[0] = setting.getDanceability();
        settings[1] = setting.getEnergy();
        settings[2] = setting.getValence();
        settings[3] = 1.0; //ideal tempo score for reference
        settings[4] = 1.0; //ideal duration score for reference
        settings[5] = 1.0; //the closer to max the song is, the more it needs to be played
        settings[6] = 0.0; //idea boo score is 0 because zeros boos is best

        settings = MathUtil.stabilize(settings);
        attributes = MathUtil.stabilize(attributes);


        //convert to probability distribution via softmax (I miss python)
        settings = MathUtil.softmax(settings);
        attributes = MathUtil.softmax(attributes);

        double crossEntropyScore = MathUtil.crossEntropy(settings, attributes);
        double entropyScore = MathUtil.crossEntropy(settings, settings);

        return 1 - entropyScore / crossEntropyScore;
    }

    public double score(Song song) {
        double[] song1 = new double[5];
        double[] song2 = new double[5];
        double attributeCoefficient = 5.0;
        double durationCoefficient = 0.2;

        song1[0] = attributeCoefficient * this.getValence();
        song2[0] = attributeCoefficient * song.getValence();

        song1[1] = attributeCoefficient * this.getEnergy();
        song2[1] = attributeCoefficient * song.getEnergy();

        song1[2] = attributeCoefficient * this.getDanceability();
        song2[2] = attributeCoefficient * song.getDanceability();

        song1[3] = durationCoefficient * this.getDuration();
        song2[3] = durationCoefficient * song.getDuration();

        song1[4] = this.getTempo();
        song2[4] = song.getTempo();


        for (int i = 3; i <= 4; i++) {
            if (song1[i] > song2[i]) {
                double songDivisor = song1[i];
                song1[i] = song1[i] / songDivisor;
                song2[i] = song2[i] / songDivisor;
            } else {
                double songDivisor = song2[i];
                song1[i] = song1[i] / songDivisor;
                song2[i] = song2[i] / songDivisor;
            }
        }

        song1 = MathUtil.stabilize(song1);
        song2 = MathUtil.stabilize(song2);

        song1 = MathUtil.softmax(song1);
        song2 = MathUtil.softmax(song2);

        double crossEntropyScore = MathUtil.crossEntropy(song1, song2);
        double entropyScore = MathUtil.crossEntropy(song1, song1);

        return 1 - (entropyScore / crossEntropyScore);
    }


    public double booScore() {
        if (this.requestedBy == null || this.requestedBy.size() == 0) {
            return 0.0;
        }

        User user = this.requestedBy.stream()
                .map(UUID::toString)
                .map(id -> UserController.getUserFromId(id))
                .max(Comparator.comparing(User::getNumBoos)).get();

        double boos = (double) user.getNumBoos();
        double numPartiers = (double) SessionController.getSessionFromId(user.getSessionId().toString()).getPartierIds().size();
        double songsAdded = (double) user.getNumSongsAdded();

        return boos / ((numPartiers+1) * songsAdded);
    }


    public double durationScore(Setting setting) {
        double max = setting.getMaxSongLength();
        double min = setting.getMinSongLength();
        double duration = this.getDuration();

        if (duration > max || duration < min) {
            return 0.0; //worst possible score
        }

        double middle = max - (max - min) / 2;
        double difference = max - middle; // half of range

        // distance from middle (THIS IS *FROM THE MIDDLE* THIS TIME !!)
        duration = Math.abs(duration - middle);

        //normalize over ONLY HALF OF THE TRUE RANGE
        duration = duration / difference;

        //we do this since we want a higher score the CLOSER we are to the middle.
        //currently, we penalize for proximity since duration - middle is lower the closer they are.
        //thus we do 1 - duration.
        return 1 - duration;
    }
}
