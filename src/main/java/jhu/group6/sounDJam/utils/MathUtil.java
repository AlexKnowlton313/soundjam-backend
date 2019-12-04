package jhu.group6.sounDJam.utils;

public class MathUtil {

    private static double epsilon = .0001;

    public static double crossEntropy(double[] dist1, double[] dist2) {
        double sum = 0.0;

        for (int i = 0; i < dist1.length; i++) {
            double product = dist1[i] * Math.log(dist2[i]);
            sum += product;
        }

        return -1 * sum;
    }

    public static double[] stabilize(double[] dist) {
        for (int i = 0; i < dist.length; i++) {
            if (dist[i] < 0.0001) {
                dist[i] += epsilon;
            }
        }
        return dist;
    }

    public static double[] softmax(double[] scores) {
        double sum = 0.0;
        for (int i = 0; i < scores.length; i++) {
            scores[i] = Math.exp(scores[i]);
            sum += scores[i];
        }

        for (int i = 0; i < scores.length; i++) {
            scores[i]  = scores[i] / sum;
        }

        return scores;
    }

}
