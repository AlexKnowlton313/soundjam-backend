package jhu.group6.sounDJam.controllers;

import io.javalin.Context;
import jhu.group6.sounDJam.Server;
import jhu.group6.sounDJam.exceptions.InvalidSessionIdException;
import jhu.group6.sounDJam.models.Queue;
import jhu.group6.sounDJam.models.Setting;
import jhu.group6.sounDJam.models.Song;
import jhu.group6.sounDJam.utils.CollectionNames;

import java.io.IOException;
import java.util.*;

import static jhu.group6.sounDJam.controllers.SettingController.getSettingFromContext;
import static jhu.group6.sounDJam.controllers.SongController.getSongFromContext;
import static jhu.group6.sounDJam.controllers.SpotifyController.getAudioFeaturesFromSongId;
import static jhu.group6.sounDJam.controllers.UserController.getUserFromContext;

public class QueueController {
    private static final double SONG_SCORE_WEIGHT = 1;

    public static void getQueue(Context context) {
        var queue = getQueueFromContext(context);
        context.json(queue.getSongs());
        context.status(200);
    }

    private static ArrayList<Song> parseGraph(Map<Song, ArrayList<Map.Entry<Song, Double>>> graph, Song current) {
        ArrayList<Song> ordered = new ArrayList<>();
        ArrayList<Song> visited = new ArrayList<>();
        Set<Song> songs = graph.keySet();

        ordered.add(current);
        while(visited.size() != songs.size()) {
            visited.add(current);
            if (visited.size() != songs.size()) {
                ArrayList<Map.Entry<Song, Double>> outgoingEdges = graph.get(current);
                Song maxSong = getClosestSong(outgoingEdges, visited);
                ordered.add(maxSong);
                current = maxSong;
            }
        }

        return ordered;
    }

    private static Song getClosestSong(ArrayList<Map.Entry<Song, Double>> outgoingEdges, List<Song> visited) {
        double maxScore = Double.MIN_VALUE;
        Song maxSong = null;
        for(Map.Entry p : outgoingEdges) {
            Song s = (Song) p.getKey();
            double songScore = (double) p.getValue();
            if (!visited.contains(s) && songScore > maxScore) {
                maxScore = songScore;
                maxSong = s;
            }
        }
        return maxSong;
    }

    private static Map<Song, ArrayList<Map.Entry<Song, Double>>> createGraph(List<Song> songs, Setting setting) {
        Map<Song, ArrayList<Map.Entry<Song, Double>>> graph = new HashMap<>();
        for (Song song : songs) {
            song.setScore(song.score(setting));
        }
        for (Song song1 : songs) {
            ArrayList<Map.Entry<Song, Double>> edges = new ArrayList<>();
            for (Song song2 : songs) {
                if (song1 == song2) {
                    continue;
                }
                double songPairScore = song1.score(song2);
//                System.out.println(song1.getName() + "  to " + song2.getName() + " = " + songPairScore);
                songPairScore += SONG_SCORE_WEIGHT * song2.getScore();
                Map.Entry<Song, Double> pair = new AbstractMap.SimpleEntry<>(song2, songPairScore);
                edges.add(pair);
            }
            graph.put(song1, edges);
        }

        return graph;
    }

    public static ArrayList<Song> curateQueue(Queue queue, Setting setting) {
        //create graph with all weighted edges
        queue.adjustTimeScore();
        Map<Song, ArrayList<Map.Entry<Song, Double>>> graph = createGraph(queue.getSongs(), setting);

        //get current song
        Song currentSong = queue.getSongs().get(0);
        ArrayList<Song> orderedList = parseGraph(graph, currentSong);
        return orderedList;
    }

    public static void recommendSong(Context context) throws IOException {
        var user = getUserFromContext(context);
        var queue = getQueueFromContext(context);
        var song = getSongFromContext(context);
        var setting = getSettingFromContext(context);
        var audioFeatures = getAudioFeaturesFromSongId(context, song.getSpotifySongId());
        song.setAudioFeatures(audioFeatures);

        queue.recommendSong(song, user);
        queue.setSongs(curateQueue(queue, setting));

        Server.getMongoRepository().updateOneFromCollectionBySessionId(
                CollectionNames.QUEUE,
                context.pathParam("session-id"),
                queue.toDocument());
        context.status(202);
    }

    public static Queue getQueueFromContext(Context context) {
        if (context.sessionAttribute("queue") == null) {
            var sessionId = context.pathParam("session-id");
            context.sessionAttribute("queue", getQueueFromId(sessionId));
        }

        return context.sessionAttribute("queue");
    }

    static Queue getQueueFromId(String sessionId) {
        var queueDoc = Server.getMongoRepository().findOneFromCollectionBySessionId(CollectionNames.QUEUE, sessionId);
        if (queueDoc == null) throw new InvalidSessionIdException(sessionId);
        return Queue.fromDocument(queueDoc);
    }

    /*
        ATTENTION: Please Note, this is for purposes of visualization during the demo for this project. We understand
        that this code is by no means robust but is purely for the sake of getting a working visualization of our
        internal algorithm.
    */
    public static void getGraph(Context context) {
        var queue = getQueueFromContext(context);
        var setting = getSettingFromContext(context);

        Map<Song, ArrayList<Map.Entry<Song, Double>>> graph = createGraph(queue.getSongs(), setting);
        Song currentSong = queue.getSongs().get(0);
        Map<String, ArrayList<Map.Entry<String, Double>>> jsonGraph = createStringGraph(graph, currentSong.getName());
        context.json(jsonGraph);
        context.status(200);
    }

    private static Map<String, ArrayList<Map.Entry<String, Double>>> createStringGraph(Map<Song, ArrayList<Map.Entry<Song, Double>>> g, String currentSong) {
        Map<String, ArrayList<Map.Entry<String, Double>>> newGraph = new HashMap<>();
        for(Song key : g.keySet()) {
            ArrayList<Map.Entry<String, Double>> newEdges = new ArrayList<>();
            for(Map.Entry p : g.get(key)) {
                Map.Entry newP = new AbstractMap.SimpleEntry(((Song) p.getKey()).getName(), p.getValue());
                newEdges.add(newP);
            }
            newGraph.put(key.getName(), newEdges);
        }
        ArrayList<Map.Entry<String, Double>> temp = new ArrayList<>();
        Map.Entry tempCurr = new AbstractMap.SimpleEntry(currentSong, 0);
        temp.add(tempCurr);
        newGraph.put("Current_Song_Playing", temp);
        return newGraph;
    }
}
