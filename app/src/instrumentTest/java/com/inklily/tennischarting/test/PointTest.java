package com.inklily.tennischarting.test;

import junit.framework.TestCase;
import com.inklily.tennischarting.Point;

public class PointTest extends TestCase {

	public void testToString() {
		Point p = new Point(true, 1, "abc");
		assertEquals("abc", p.toString());
		p.setPoint("cba");
		assertEquals("cba", p.toString());
	}

	public void testTrivialWinners() {
		assertEquals(1, new Point(true, 1, "S").winner());
		assertEquals(2, new Point(true, 1, "R").winner());
		assertEquals(1, new Point(true, 1, "P").winner());
		assertEquals(2, new Point(true, 1, "Q").winner());

		assertEquals(2, new Point(true, 2, "S").winner());
		assertEquals(1, new Point(true, 2, "R").winner());
		assertEquals(2, new Point(true, 2, "P").winner());
		assertEquals(1, new Point(true, 2, "Q").winner());
	}

	public void testServe() {
		assertEquals(1, new Point(true, 1, "4*").winner());
		assertFalse(new Point(true, 1, "4*").isFault());
		assertEquals(1, new Point(true, 1, "5#").winner());
		assertFalse(new Point(true, 1, "5#").isFault());
		assertEquals(2, new Point(true, 1, "6w").winner());
		assertTrue(new Point(true, 1, "6w").isFault());

		assertEquals(1, new Point(true, 2, "4e").winner());
		assertTrue(new Point(true, 2, "4e").isFault());
		assertEquals(1, new Point(true, 2, "5x").winner());
		assertTrue(new Point(true, 2, "5x").isFault());
		assertEquals(1, new Point(true, 2, "6d").winner());
		assertTrue(new Point(true, 2, "6d").isFault());
		assertEquals(1, new Point(true, 2, "6!").winner());
		assertTrue(new Point(true, 2, "6!").isFault());
		assertEquals(1, new Point(true, 2, "6n").winner());
		assertTrue(new Point(true, 2, "6n").isFault());
	}

	public void testReturnShot() {
		assertEquals(2, new Point(true, 1, "4f*").winner());
		assertEquals(2, new Point(true, 1, "4b*").winner());

		assertEquals(2, new Point(true, 1, "4r*").winner());
		assertEquals(2, new Point(true, 1, "4s*").winner());

		assertEquals(2, new Point(true, 1, "4v*").winner());
		assertEquals(2, new Point(true, 1, "4z*").winner());

		assertEquals(2, new Point(true, 1, "4o*").winner());
		assertEquals(2, new Point(true, 1, "4p*").winner());

		assertEquals(1, new Point(true, 2, "4u*").winner());
		assertEquals(1, new Point(true, 2, "4y*").winner());

		assertEquals(1, new Point(true, 2, "4l*").winner());
		assertEquals(1, new Point(true, 2, "4m*").winner());

		assertEquals(1, new Point(true, 2, "4h*").winner());
		assertEquals(1, new Point(true, 2, "4i*").winner());

		assertEquals(1, new Point(true, 2, "4j*").winner());
		assertEquals(1, new Point(true, 2, "4k*").winner());

		assertEquals(1, new Point(true, 2, "4t*").winner());
		assertEquals(1, new Point(true, 2, "4q*").winner());
	}

	public void testReturnErrors() {
		assertEquals(1, new Point(true, 1, "4f#").winner());
		assertEquals(1, new Point(true, 1, "4b8#").winner());

		assertEquals(1, new Point(true, 1, "4r28#").winner());
		assertEquals(1, new Point(true, 1, "4s28@").winner());

		assertEquals(1, new Point(true, 1, "4v1e@").winner());
		assertEquals(1, new Point(true, 1, "4z1e#").winner());

		assertEquals(1, new Point(true, 1, "4o2w@").winner());
		assertEquals(1, new Point(true, 1, "4p2d@").winner());

		assertEquals(2, new Point(true, 2, "4u2x@").winner());
		assertEquals(2, new Point(true, 2, "4y2!@").winner());

		assertEquals(2, new Point(true, 2, "4l2n@").winner());
		assertEquals(2, new Point(true, 2, "4m3n#").winner());

		assertEquals(2, new Point(true, 2, "4hn#").winner());
		assertEquals(2, new Point(true, 2, "4i#").winner());

		assertEquals(2, new Point(true, 2, "4j29e#").winner());
		assertEquals(2, new Point(true, 2, "4k17!@").winner());

		assertEquals(2, new Point(true, 2, "4t28e@").winner());
		assertEquals(2, new Point(true, 2, "4q18#").winner());
	}

	public void testPointLength() {
		assertEquals(1, new Point(true, 1, "4*").shotCount());
		assertEquals(2, new Point(true, 1, "4f*").shotCount());
		assertEquals(3, new Point(true, 1, "4fb*").shotCount());

		assertEquals(4, new Point(true, 1, "4fbr*").shotCount());
		assertEquals(5, new Point(true, 1, "4bffs*").shotCount());

		assertEquals(6, new Point(true, 1, "4r2ssv2v*").shotCount());
		assertEquals(7, new Point(true, 1, "4srr2o09pze@").shotCount());
	}

	public void testLongerPoints() {
		assertEquals(2, new Point(true, 1, "4f*").winner());
		assertEquals(1, new Point(true, 1, "4fb*").winner());

		assertEquals(2, new Point(true, 1, "4fbr*").winner());
		assertEquals(1, new Point(true, 1, "4bffs*").winner());

		assertEquals(2, new Point(true, 1, "4r2ssv2v*").winner());
		assertEquals(1, new Point(true, 1, "4srr2o3pz*").winner());

		assertEquals(1, new Point(true, 1, "4f#").winner());
		assertEquals(2, new Point(true, 1, "4fb@").winner());

		assertEquals(1, new Point(true, 1, "4fbre").winner());
		assertEquals(2, new Point(true, 1, "4bffsw").winner());

		assertEquals(1, new Point(true, 1, "4r2ssv2v2x@").winner());
		assertEquals(2, new Point(true, 1, "4srr28o39pz39!#").winner());
	}

	public void testValidPoints() {
		assertTrue(new Point(true, 2, "4*").isValid());
		assertTrue(new Point(true, 2, "5#").isValid());
		assertTrue(new Point(true, 2, "6w").isValid());
		assertTrue(new Point(true, 2, "4f39*").isValid());

		assertTrue(new Point(true, 2, "4f1b2#").isValid());
		assertTrue(new Point(true, 2, "4f1b2e").isValid());
		assertTrue(new Point(true, 2, "4f1b2@").isValid());
		assertTrue(new Point(true, 2, "4f1b2e").isValid());
		assertTrue(new Point(true, 2, "4r3s1#").isValid());
		assertTrue(new Point(true, 2, "4v3z2#").isValid());
		assertTrue(new Point(true, 2, "4o3p0#").isValid());
		assertTrue(new Point(true, 2, "4u2y3#").isValid());
		assertTrue(new Point(true, 2, "4l1m2#").isValid());
		assertTrue(new Point(true, 2, "4h3i1#").isValid());
		assertTrue(new Point(true, 2, "4j2k3#").isValid());
		assertTrue(new Point(true, 2, "4t3q#").isValid());
		assertTrue(new Point(true, 2, "4q*").isValid());
		assertTrue(new Point(true, 2, "5q3*").isValid());

		assertTrue(new Point(true, 2, "4fbrsvzopuylmhijktq#").isValid());
		assertTrue(new Point(true, 2, "4fbrsvzopuylmhijktqe@").isValid());

		assertTrue(new Point(true, 2, "4f1b2r3s1v3z2o3p0u2y3l1m2h3i1j2k3t3q#").isValid());
		assertTrue(new Point(true, 2, "4f1b2r3s1v3z2o3p1u2y3l1m2h3i1j2k3t3q2w@").isValid());

		assertTrue(new Point(true, 2, "4f1b2r39s1v38z2o38p1u2y3l1m2h3i1j2k3qt1w#").isValid());
		assertTrue(new Point(true, 2, "4f18b28r38s1v38z27o37p18u27y37l18m27h39i17j28k39t37q29t19x@").isValid());
	}

	public void testInvalidPoints() {
		assertFalse(new Point(true, 2, "abc").isValid());
		assertFalse(new Point(true, 2, "%*$#%$").isValid());
		assertFalse(new Point(true, 2, "4#@").isValid());
		assertFalse(new Point(true, 2, "4f").isValid());
		assertFalse(new Point(true, 2, "4fbrsvzopuylmhijktq").isValid());
		assertFalse(new Point(true, 2, "*").isValid());
		assertFalse(new Point(true, 2, "2*").isValid());
		assertFalse(new Point(true, 2, "f*").isValid());
		assertFalse(new Point(true, 2, "4f93*").isValid());
		assertFalse(new Point(true, 2, "9f39*").isValid());
	}

    public void testShotCounts() {
        assertEquals(0, new Point(true, 1, "").shotCount());
        assertEquals(1, new Point(true, 1, "P").shotCount());
        assertEquals(1, new Point(true, 1, "3").shotCount());
        assertEquals(2, new Point(true, 1, "4b").shotCount());
        assertEquals(2, new Point(true, 1, "5f1").shotCount());
        assertEquals(3, new Point(true, 1, "5f1b").shotCount());
        assertEquals(3, new Point(true, 1, "5f1b4e@").shotCount());
        assertEquals(1, new Point(true, 1, "ccc4").shotCount());
        assertEquals(2, new Point(true, 1, "ccccc4b").shotCount());
    }
}
