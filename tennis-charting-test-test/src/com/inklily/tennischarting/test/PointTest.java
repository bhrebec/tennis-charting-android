package com.inklily.tennischarting.test;

import junit.framework.TestCase;
import com.inklily.tennischarting.Point;

public class PointTest extends TestCase {

	public void testToString() {
		Point p = new Point(true, 1, "abc");
		assertEquals(p.toString(), "abc");
		p.setPoint("cba");
		assertEquals(p.toString(), "cba");
	}

	public void testTrivialWinners() {
		assertEquals(new Point(true, 1, "S").winner(), 1);
		assertEquals(new Point(true, 1, "R").winner(), 2);
		assertEquals(new Point(true, 1, "P").winner(), 1);
		assertEquals(new Point(true, 1, "Q").winner(), 2);

		assertEquals(new Point(true, 2, "S").winner(), 2);
		assertEquals(new Point(true, 2, "R").winner(), 1);
		assertEquals(new Point(true, 2, "P").winner(), 2);
		assertEquals(new Point(true, 2, "Q").winner(), 1);
	}

	public void testServe() {
		assertEquals(new Point(true, 1, "4*").winner(), 1);
		assertFalse(new Point(true, 1, "4*").fault());
		assertEquals(new Point(true, 1, "5#").winner(), 1);
		assertFalse(new Point(true, 1, "5#").fault());
		assertEquals(new Point(true, 1, "6w").winner(), 2);
		assertTrue(new Point(true, 1, "6w").fault());

		assertEquals(new Point(true, 2, "4e").winner(), 1);
		assertTrue(new Point(true, 2, "4e").fault());
		assertEquals(new Point(true, 2, "5x").winner(), 1);
		assertTrue(new Point(true, 2, "5x").fault());
		assertEquals(new Point(true, 2, "6d").winner(), 1);
		assertTrue(new Point(true, 2, "6d").fault());
		assertEquals(new Point(true, 2, "6!").winner(), 1);
		assertTrue(new Point(true, 2, "6!").fault());
		assertEquals(new Point(true, 2, "6n").winner(), 1);
		assertTrue(new Point(true, 2, "6n").fault());
	}

	public void testReturnShot() {
		assertEquals(new Point(true, 1, "4f*").winner(), 2);
		assertEquals(new Point(true, 1, "4b*").winner(), 2);

		assertEquals(new Point(true, 1, "4r*").winner(), 2);
		assertEquals(new Point(true, 1, "4s*").winner(), 2);

		assertEquals(new Point(true, 1, "4v*").winner(), 2);
		assertEquals(new Point(true, 1, "4z*").winner(), 2);

		assertEquals(new Point(true, 1, "4o*").winner(), 2);
		assertEquals(new Point(true, 1, "4p*").winner(), 2);

		assertEquals(new Point(true, 2, "4u*").winner(), 1);
		assertEquals(new Point(true, 2, "4y*").winner(), 1);

		assertEquals(new Point(true, 2, "4l*").winner(), 1);
		assertEquals(new Point(true, 2, "4m*").winner(), 1);

		assertEquals(new Point(true, 2, "4h*").winner(), 1);
		assertEquals(new Point(true, 2, "4i*").winner(), 1);

		assertEquals(new Point(true, 2, "4j*").winner(), 1);
		assertEquals(new Point(true, 2, "4k*").winner(), 1);

		assertEquals(new Point(true, 2, "4t*").winner(), 1);
		assertEquals(new Point(true, 2, "4q*").winner(), 1);
	}

	public void testReturnErrors() {
		assertEquals(new Point(true, 1, "4f#").winner(), 1);
		assertEquals(new Point(true, 1, "4b8#").winner(), 1);

		assertEquals(new Point(true, 1, "4r28#").winner(), 1);
		assertEquals(new Point(true, 1, "4s28@").winner(), 1);

		assertEquals(new Point(true, 1, "4v1e@").winner(), 1);
		assertEquals(new Point(true, 1, "4z1e#").winner(), 1);

		assertEquals(new Point(true, 1, "4o2w@").winner(), 1);
		assertEquals(new Point(true, 1, "4p2d@").winner(), 1);

		assertEquals(new Point(true, 2, "4u2x@").winner(), 2);
		assertEquals(new Point(true, 2, "4y2!@").winner(), 2);

		assertEquals(new Point(true, 2, "4l2n@").winner(), 2);
		assertEquals(new Point(true, 2, "4m3n#").winner(), 2);

		assertEquals(new Point(true, 2, "4hn#").winner(), 2);
		assertEquals(new Point(true, 2, "4i#").winner(), 2);

		assertEquals(new Point(true, 2, "4j29e#").winner(), 2);
		assertEquals(new Point(true, 2, "4k17!@").winner(), 2);

		assertEquals(new Point(true, 2, "4t28e@").winner(), 2);
		assertEquals(new Point(true, 2, "4q18#").winner(), 2);
	}

	public void testPointLength() {
		assertEquals(new Point(true, 1, "4*").shotCount(), 1);
		assertEquals(new Point(true, 1, "4f*").shotCount(), 2);
		assertEquals(new Point(true, 1, "4fb*").shotCount(), 3);

		assertEquals(new Point(true, 1, "4fbr*").shotCount(), 4);
		assertEquals(new Point(true, 1, "4bffs*").shotCount(), 5);

		assertEquals(new Point(true, 1, "4r2ssv2v*").shotCount(), 6);
		assertEquals(new Point(true, 1, "4srr2o09pze@").shotCount(), 7);
	}

	public void testLongerPoints() {
		assertEquals(new Point(true, 1, "4f*").winner(), 2);
		assertEquals(new Point(true, 1, "4fb*").winner(), 1);

		assertEquals(new Point(true, 1, "4fbr*").winner(), 2);
		assertEquals(new Point(true, 1, "4bffs*").winner(), 1);

		assertEquals(new Point(true, 1, "4r2ssv2v*").winner(), 2);
		assertEquals(new Point(true, 1, "4srr2o3pz*").winner(), 1);

		assertEquals(new Point(true, 1, "4f#").winner(), 1);
		assertEquals(new Point(true, 1, "4fb@").winner(), 2);

		assertEquals(new Point(true, 1, "4fbre").winner(), 1);
		assertEquals(new Point(true, 1, "4bffsw").winner(), 2);

		assertEquals(new Point(true, 1, "4r2ssv2v2x@").winner(), 1);
		assertEquals(new Point(true, 1, "4srr28o39pz39!#").winner(), 2);
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
}
