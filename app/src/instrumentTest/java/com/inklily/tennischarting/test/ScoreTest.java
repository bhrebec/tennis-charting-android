package com.inklily.tennischarting.test;

import com.inklily.tennischarting.Point;
import com.inklily.tennischarting.Score;

import junit.framework.Assert;
import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

/**
 * Created by mrdog on 2/16/14.
 */
public class ScoreTest extends TestCase {
    private Score score3;
    private Score score3tb;
    private Score score5;
    private Score score5tb;

    private Score[] allScores;

    public static void assertEquals(String expected, String actual) {
        try {
            Assert.assertEquals(expected, actual);
        } catch (ComparisonFailure e) {
            throw new ComparisonFailure(String.format(
                    "expected: \"%s\", was \"%s\"", expected, actual),
                    expected, actual);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        score3 = new Score(3, false);
        score3tb = new Score(3, true);
        score5 = new Score(5, false);
        score5tb = new Score(5, true);

        allScores = new Score[] {score3, score3tb, score5, score5tb};
    }

    private void addPointsFor(Score score, int count, Point.PointGiven winner) {
        for (int i = 0; i < count; i++) {
            int server = score.server();
            Point p = new Point(true, server);
            p.givePoint(winner);

            score.score_point(p);
        }
    }

    private void addPointsForPlayer(Score score, int count, int winner) {
        for (int i = 0; i < count; i++) {
            int server = score.server();
            Point p = new Point(true, server);
            if (server == winner)
                p.givePoint(Point.PointGiven.POINT_SERVER);
            else
                p.givePoint(Point.PointGiven.POINT_RETURNER);

            score.score_point(p);
        }
    }

    public void testScore() {
        assertEquals("0-0:0-0", score3.toString());

        addPointsForPlayer(score3, 4, 1);
        assertEquals("1-0:0-0", score3.toString());

        addPointsForPlayer(score3, 4, 1);
        assertEquals("2-0:0-0", score3.toString());

        addPointsForPlayer(score3, 4, 2);
        assertEquals("2-1:0-0", score3.toString());

        addPointsForPlayer(score3, 4, 2);
        assertEquals("2-2:0-0", score3.toString());

        addPointsFor(score3, 4 * 4 * 2, Point.PointGiven.POINT_SERVER);
        assertEquals("6-6:0-0", score3.toString());

        addPointsForPlayer(score3, 7, 1);
        assertEquals("7-6(0) 0-0:0-0", score3.toString());
    }

    public void testScorePts() {
        assertEquals("0-0:0-0", score3.toString());

        addPointsForPlayer(score3, 1, 1);
        assertEquals("0-0:15-0", score3.toString());

        addPointsFor(score3, 1, Point.PointGiven.POINT_SERVER);
        assertEquals("0-0:30-0", score3.toString());

        addPointsForPlayer(score3, 1, 2);
        assertEquals("0-0:30-15", score3.toString());

        addPointsForPlayer(score3, 1, 2);
        assertEquals("0-0:30-30", score3.toString());

        addPointsForPlayer(score3, 1, 1);
        assertEquals("0-0:40-30", score3.toString());

        addPointsForPlayer(score3, 1, 1);
        assertEquals("1-0:0-0", score3.toString());

        addPointsForPlayer(score3, 1, 2);
        assertEquals("1-0:15-0", score3.toString());
    }

    public void testScore2Sets() {
        addPointsForPlayer(score3, 4 * 6, 1);
        assertEquals("6-0 0-0:0-0", score3.toString());

        addPointsForPlayer(score3, 4, 1);
        assertEquals("6-0 1-0:0-0", score3.toString());

        addPointsForPlayer(score3, 4, 1);
        assertEquals("6-0 2-0:0-0", score3.toString());
    }
    public void testServerId() {
        for (Score sc : allScores)
            assertEquals(sc.toString(), 1, sc.server());

        for (Score sc : allScores) {
            addPointsFor(sc, 4, Point.PointGiven.POINT_SERVER);
            assertEquals(sc.toString(), 2, sc.server());
        }

        for (Score sc : allScores) {
            addPointsFor(sc, 4, Point.PointGiven.POINT_RETURNER);
            assertEquals(sc.toString(), 1, sc.server());
        }
    }

    public void testTb() {
        // Get to TB
        for (Score sc : allScores) {
            addPointsFor(sc, 4*12, Point.PointGiven.POINT_SERVER);
        }

        assertTrue(score3.toString(), score3.in_tb());
        assertTrue(score5.toString(), score5.in_tb());
        assertTrue(score3tb.toString(), score3tb.in_tb());
        assertTrue(score5tb.toString(), score5tb.in_tb());
    }

    public void testFinalTb() {
        // Test 3 setters
        addPointsForPlayer(score3, 4 * 6 * 2, 1);
        addPointsForPlayer(score3tb, 4 * 6 * 2, 1);
        addPointsFor(score3, 4 * 12, Point.PointGiven.POINT_SERVER);
        addPointsFor(score3tb, 4 * 12, Point.PointGiven.POINT_SERVER);

        assertFalse(score3.toString(), score3.in_tb());
        assertTrue(score3.toString(), score3tb.in_tb());

        // Test 5 setters
        addPointsForPlayer(score5, 4 * 6 * 4, 1);
        addPointsForPlayer(score5tb, 4 * 6 * 4, 1);
        addPointsFor(score5, 4 * 12, Point.PointGiven.POINT_SERVER);
        addPointsFor(score5tb, 4 * 12, Point.PointGiven.POINT_RETURNER);

        assertFalse(score5.toString(), score5.in_tb());
        assertTrue(score5tb.toString(), score5tb.in_tb());
    }

    public void testTbServer() {
        // Get to TB
        for (Score sc : allScores) {
            addPointsFor(sc, 4*12, Point.PointGiven.POINT_SERVER);
        }

        assertEquals(score3.toString(), 1, score3.server());
        assertEquals(score5.toString(), 1, score5.server());
        assertEquals(score3tb.toString(), 1, score3tb.server());
        assertEquals(score5tb.toString(), 1, score5tb.server());

        for (Score sc : allScores) {
            addPointsFor(sc, 1, Point.PointGiven.POINT_SERVER);
        }

        assertEquals(score3.toString(), 2, score3.server());
        assertEquals(score5.toString(), 2, score5.server());
        assertEquals(score3tb.toString(), 2, score3tb.server());
        assertEquals(score5tb.toString(), 2, score5tb.server());

        for (Score sc : allScores) {
            addPointsFor(sc, 1, Point.PointGiven.POINT_SERVER);
        }

        assertEquals(score3.toString(), 2, score3.server());
        assertEquals(score5.toString(), 2, score5.server());
        assertEquals(score3tb.toString(), 2, score3tb.server());
        assertEquals(score5tb.toString(), 2, score5tb.server());

        for (Score sc : allScores) {
            addPointsFor(sc, 1, Point.PointGiven.POINT_SERVER);
        }

        assertEquals(score3.toString(), 1, score3.server());
        assertEquals(score5.toString(), 1, score5.server());
        assertEquals(score3tb.toString(), 1, score3tb.server());
        assertEquals(score5tb.toString(), 1, score5tb.server());
    }

    public void testIsCompleteTB() {
        // 3rd set
        addPointsForPlayer(score3tb, 4 * 6 * 2, 1);

        // 5th set
        addPointsForPlayer(score5tb, 4 * 6 * 4, 1);

        // Get to TB
        addPointsFor(score3tb, 4*12, Point.PointGiven.POINT_SERVER);
        addPointsFor(score5tb, 4*12, Point.PointGiven.POINT_SERVER);

        // Mini-break
        addPointsFor(score3tb, 1, Point.PointGiven.POINT_SERVER);
        addPointsFor(score5tb, 1, Point.PointGiven.POINT_SERVER);
        addPointsFor(score3tb, 1, Point.PointGiven.POINT_RETURNER);
        addPointsFor(score5tb, 1, Point.PointGiven.POINT_RETURNER);

        // With serve until it ends
        addPointsFor(score3tb, 10, Point.PointGiven.POINT_SERVER);
        addPointsFor(score5tb, 10, Point.PointGiven.POINT_SERVER);

        assertTrue(score3tb.toString(), score3tb.isComplete());
        assertTrue(score5tb.toString(), score5tb.isComplete());
    }

    public void testIsComplete() {
        // 3rd set
        addPointsForPlayer(score3, 4 * 6 * 3, 1);
        addPointsForPlayer(score3tb, 4 * 6 * 3, 1);

        // 5th set
        addPointsForPlayer(score5, 4 * 6 * 5, 2);
        addPointsForPlayer(score5tb, 4 * 6 * 5, 2);

        assertTrue(score3.toString(), score3.isComplete());
        assertTrue(score5.toString(), score5.isComplete());
        assertTrue(score3tb.toString(), score3tb.isComplete());
        assertTrue(score5tb.toString(), score5tb.isComplete());
    }

    public void testDeuceCourt() {
        assertTrue(score3.deuceCourt());

        addPointsFor(score3, 1, Point.PointGiven.POINT_SERVER);
        assertFalse(score3.toString(), score3.deuceCourt());

        addPointsFor(score3, 1, Point.PointGiven.POINT_SERVER);
        assertTrue(score3.toString(), score3.deuceCourt());

        addPointsFor(score3, 1, Point.PointGiven.POINT_SERVER);
        assertFalse(score3.toString(), score3.deuceCourt());

        addPointsFor(score3, 1, Point.PointGiven.POINT_SERVER);
        assertTrue(score3.toString(), score3.deuceCourt());
    }

    public void testNear() {
        // 0-0
        assertFalse(score3.near());

        // 1-0
        addPointsFor(score3, 4, Point.PointGiven.POINT_SERVER);
        assertTrue(score3.toString(), score3.near());

        // 1-1
        addPointsFor(score3, 4, Point.PointGiven.POINT_SERVER);
        assertTrue(score3.toString(), score3.near());

        // 2-1
        addPointsFor(score3, 4, Point.PointGiven.POINT_SERVER);
        assertFalse(score3.toString(), score3.near());

        // 2-2
        addPointsFor(score3, 4, Point.PointGiven.POINT_SERVER);
        assertFalse(score3.toString(), score3.near());
    }

    public void testNearTb() {
        // 6-6, 0-0
        addPointsFor(score3, 4 * 12, Point.PointGiven.POINT_SERVER);
        assertFalse(score3.toString(), score3.near());

        // 6-6, 3-3
        addPointsFor(score3, 6, Point.PointGiven.POINT_SERVER);
        assertTrue(score3.toString(), score3.near());

        // 6-6, 6-6
        addPointsFor(score3, 6, Point.PointGiven.POINT_SERVER);
        assertFalse(score3.toString(), score3.near());

        // 6-6, 9-9
        addPointsFor(score3, 6, Point.PointGiven.POINT_SERVER);
        assertTrue(score3.toString(), score3.near());
    }
}
