package tests.utils;

import jhu.group6.sounDJam.utils.MathUtil;
import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

public class MathUtilTest {

    @Test
    public void testSoftmax() {
        Random rand = new Random();
        int size = rand.nextInt(20) + 10;

        double[] dist1 = new double[size];

        for (int i = 0; i < size; i++) {
            dist1[i] = rand.nextInt(10);
        }

        dist1 = MathUtil.softmax(dist1);

        for (int i = 0; i < size; i++) {
            assertTrue(dist1[i] >= 0.0 && dist1[i] <= 1.0);
        }

        double sum = 0.0;
        for (int i = 0; i < size; i++) {
            sum += dist1[i];
        }
        assertEquals(1.0, sum, .00001);
    }

    @Test
    public void testCrossEntropy() {
        Random rand = new Random();
        int size = rand.nextInt(20) + 10;

        double[] dist1 = new double[size];
        double[] dist2 = new double[size];

        for (int i = 0; i < size; i++) {
            dist1[i] = rand.nextInt(10);
            dist2[i] = rand.nextInt(10);
        }
        dist1[0] += 19.3; //just to ensure the two are indeed different

        dist1 = MathUtil.softmax(dist1);
        dist2 = MathUtil.softmax(dist2);

        double score = MathUtil.crossEntropy(dist1, dist2);
        double controlScore = MathUtil.crossEntropy(dist1, dist1);
        assertTrue(score > controlScore);
    }

}