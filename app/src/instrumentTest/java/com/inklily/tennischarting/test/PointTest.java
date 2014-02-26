package com.inklily.tennischarting.test;

import junit.framework.TestCase;
import com.inklily.tennischarting.Point;

public class PointTest extends TestCase {

	public void testToString() {
		Point p = new Point("abc");
		assertEquals("abc", p.toString());
		p.setPoint("cba");
		assertEquals("cba", p.toString());
	}

	public void testTrivialWinners() {
		assertEquals(Point.PointWinner.SERVER, new Point("S").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("R").winner());
		assertEquals(Point.PointWinner.SERVER, new Point("P").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("Q").winner());

		assertEquals(Point.PointWinner.SERVER, new Point("S").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("R").winner());
		assertEquals(Point.PointWinner.SERVER, new Point("P").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("Q").winner());
	}

	public void testServe() {
		assertEquals(Point.PointWinner.SERVER, new Point("4*").winner());
		assertFalse(new Point("4*").isFault());
		assertEquals(Point.PointWinner.SERVER, new Point("5#").winner());
		assertFalse(new Point("5#").isFault());
		assertEquals(Point.PointWinner.RETURNER, new Point("6w").winner());
		assertTrue(new Point("6w").isFault());

		assertEquals(Point.PointWinner.RETURNER, new Point("4e").winner());
		assertTrue(new Point("4e").isFault());
		assertEquals(Point.PointWinner.RETURNER, new Point("5x").winner());
		assertTrue(new Point("5x").isFault());
		assertEquals(Point.PointWinner.RETURNER, new Point("6d").winner());
		assertTrue(new Point("6d").isFault());
		assertEquals(Point.PointWinner.RETURNER, new Point("6!").winner());
		assertTrue(new Point("6!").isFault());
		assertEquals(Point.PointWinner.RETURNER, new Point("6n").winner());
		assertTrue(new Point("6n").isFault());
	}

	public void testReturnShot() {
		assertEquals(Point.PointWinner.RETURNER, new Point("4f*").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("4b*").winner());

		assertEquals(Point.PointWinner.RETURNER, new Point("4r*").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("4s*").winner());

		assertEquals(Point.PointWinner.RETURNER, new Point("4v*").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("4z*").winner());

		assertEquals(Point.PointWinner.RETURNER, new Point("4o*").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("4p*").winner());

		assertEquals(Point.PointWinner.RETURNER, new Point("4u*").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("4y*").winner());

		assertEquals(Point.PointWinner.RETURNER, new Point("4l*").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("4m*").winner());

		assertEquals(Point.PointWinner.RETURNER, new Point("4h*").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("4i*").winner());

		assertEquals(Point.PointWinner.RETURNER, new Point("4j*").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("4k*").winner());

		assertEquals(Point.PointWinner.RETURNER, new Point("4t*").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("4q*").winner());
	}

	public void testReturnErrors() {
		assertEquals(Point.PointWinner.SERVER, new Point("4f#").winner());
		assertEquals(Point.PointWinner.SERVER, new Point("4b8#").winner());

		assertEquals(Point.PointWinner.SERVER, new Point("4r28#").winner());
		assertEquals(Point.PointWinner.SERVER, new Point("4s28@").winner());

		assertEquals(Point.PointWinner.SERVER, new Point("4v1e@").winner());
		assertEquals(Point.PointWinner.SERVER, new Point("4z1e#").winner());

		assertEquals(Point.PointWinner.SERVER, new Point("4o2w@").winner());
		assertEquals(Point.PointWinner.SERVER, new Point("4p2d@").winner());

		assertEquals(Point.PointWinner.SERVER, new Point("4u2x@").winner());
		assertEquals(Point.PointWinner.SERVER, new Point("4y2!@").winner());

		assertEquals(Point.PointWinner.SERVER, new Point("4l2n@").winner());
		assertEquals(Point.PointWinner.SERVER, new Point("4m3n#").winner());

		assertEquals(Point.PointWinner.SERVER, new Point("4hn#").winner());
		assertEquals(Point.PointWinner.SERVER, new Point("4i#").winner());

		assertEquals(Point.PointWinner.SERVER, new Point("4j29e#").winner());
		assertEquals(Point.PointWinner.SERVER, new Point("4k17!@").winner());

		assertEquals(Point.PointWinner.SERVER, new Point("4t28e@").winner());
		assertEquals(Point.PointWinner.SERVER, new Point("4q18#").winner());
	}

	public void testPointLength() {
		assertEquals(1, new Point("4*").shotCount());
		assertEquals(2, new Point("4f*").shotCount());
		assertEquals(3, new Point("4fb*").shotCount());

		assertEquals(4, new Point("4fbr*").shotCount());
		assertEquals(5, new Point("4bffs*").shotCount());

		assertEquals(6, new Point("4r2ssv2v*").shotCount());
		assertEquals(7, new Point("4srr2o09pze@").shotCount());
	}

	public void testLongerPoints() {
		assertEquals(Point.PointWinner.RETURNER, new Point("4f*").winner());
		assertEquals(Point.PointWinner.SERVER, new Point("4fb*").winner());

		assertEquals(Point.PointWinner.RETURNER, new Point("4fbr*").winner());
		assertEquals(Point.PointWinner.SERVER, new Point("4bffs*").winner());

		assertEquals(Point.PointWinner.RETURNER, new Point("4r2ssv2v*").winner());
		assertEquals(Point.PointWinner.SERVER, new Point("4srr2o3pz*").winner());

		assertEquals(Point.PointWinner.SERVER, new Point("4f#").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("4fb@").winner());

		assertEquals(Point.PointWinner.SERVER, new Point("4fbre").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("4bffsw").winner());

		assertEquals(Point.PointWinner.SERVER, new Point("4r2ssv2v2x@").winner());
		assertEquals(Point.PointWinner.RETURNER, new Point("4srr28o39pz39!#").winner());
	}

	public void testValidPoints() {
		assertTrue(new Point("4*").isValid());
		assertTrue(new Point("5#").isValid());
		assertTrue(new Point("6w").isValid());
		assertTrue(new Point("4f39*").isValid());

		assertTrue(new Point("4f1b2#").isValid());
		assertTrue(new Point("4f1b2e").isValid());
		assertTrue(new Point("4f1b2@").isValid());
		assertTrue(new Point("4f1b2e").isValid());
		assertTrue(new Point("4r3s1#").isValid());
		assertTrue(new Point("4v3z2#").isValid());
		assertTrue(new Point("4o3p0#").isValid());
		assertTrue(new Point("4u2y3#").isValid());
		assertTrue(new Point("4l1m2#").isValid());
		assertTrue(new Point("4h3i1#").isValid());
		assertTrue(new Point("4j2k3#").isValid());
		assertTrue(new Point("4t3q#").isValid());
		assertTrue(new Point("4q*").isValid());
		assertTrue(new Point("5q3*").isValid());

		assertTrue(new Point("4fbrsvzopuylmhijktq#").isValid());
		assertTrue(new Point("4fbrsvzopuylmhijktqe@").isValid());

		assertTrue(new Point("4f1b2r3s1v3z2o3p0u2y3l1m2h3i1j2k3t3q#").isValid());
		assertTrue(new Point("4f1b2r3s1v3z2o3p1u2y3l1m2h3i1j2k3t3q2w@").isValid());

		assertTrue(new Point("4f1b2r39s1v38z2o38p1u2y3l1m2h3i1j2k3qt1w#").isValid());
		assertTrue(new Point("4f18b28r38s1v38z27o37p18u27y37l18m27h39i17j28k39t37q29t19x@").isValid());
	}

	public void testInvalidPoints() {
		assertFalse(new Point("abc").isValid());
		assertFalse(new Point("%*$#%$").isValid());
		assertFalse(new Point("4#@").isValid());
		assertFalse(new Point("4f").isValid());
		assertFalse(new Point("4fbrsvzopuylmhijktq").isValid());
		assertFalse(new Point("*").isValid());
		assertFalse(new Point("2*").isValid());
		assertFalse(new Point("f*").isValid());
		assertFalse(new Point("4f93*").isValid());
		assertFalse(new Point("9f39*").isValid());
	}

    public void testShotCounts() {
        assertEquals(0, new Point("").shotCount());
        assertEquals(0, new Point("P").shotCount());
        assertEquals(0, new Point("R").shotCount());
        assertEquals(0, new Point("S").shotCount());
        assertEquals(0, new Point("Q").shotCount());
        assertEquals(1, new Point("6").shotCount());
        assertEquals(2, new Point("4b").shotCount());
        assertEquals(2, new Point("5f1").shotCount());
        assertEquals(3, new Point("5f1b").shotCount());
        assertEquals(3, new Point("5f1b4e@").shotCount());
        assertEquals(1, new Point("ccc4").shotCount());
        assertEquals(2, new Point("ccccc4b").shotCount());
    }
}
