package jhu.group6.sounDJam.utils;

public class MathUtil {

    private static double epsilon = .0001;

    public static double sigmoid(double x, double alpha) {
        return 1 / (1 + Math.exp(-1 * alpha * x));
    }

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

    public static double normalCDF(double z_score) {
        //Taylor Series Approximation of Gaussian CDF
        double sum = 0.0;
        for (int n = 0; n < 6; n++) {
            double numerator = Math.pow(-1, n);
            double factorial = 1;
            for (int i = 1; i <= n; i++) {
                factorial *= i;
            }
            double denominator = factorial * Math.pow(2, n) * (2*n + 1);
            double z_term = Math.pow(z_score, 2*n + 1);
            sum += (numerator / denominator) * z_term;
        }

        return (1 / Math.sqrt(2*Math.PI)) * sum + 0.5;
    }

}
