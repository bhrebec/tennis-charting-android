package com.inklily.tennischarting;

import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Point {
	private StringBuilder data;

	private boolean mFirstServe;
	private int mServer;
	public String comments;
	
	// TODO: build these from the enums?
	private static final String VALID_SHOTS = "frvoulhjbszpymiktq";
	private static final String SERVE_PATTERN_STRING = "^[0456]";
	private static final String RALLY_PATTERN_STRING = "[" + VALID_SHOTS + "][0123]?[0789]?";
	private static final String ENDING_PATTERN_STRING = "([*e#@nwdx!]|[nwdx!e][#@])$";
	private static final Pattern ENDING_PATTERN = Pattern.compile(ENDING_PATTERN_STRING);
	private static final Pattern SHOT_PATTERN = Pattern.compile("[" + VALID_SHOTS + "]");
	private static final Pattern POINT_PATTERN = Pattern.compile("P|Q|R|S|" + SERVE_PATTERN_STRING 
			+ "(" + RALLY_PATTERN_STRING + ")*" + ENDING_PATTERN_STRING);

	public enum PointGiven {
		POINT_SERVER('S'),
		POINT_RETURNER('R'),
		POINT_SERVER_PENALTY('P'),
		POINT_RETURNER_PENALTY('Q');

		private char value;
		private PointGiven(char v) { value = v; }
		public char asChar() { return value; }
		public String toString() { return Character.toString(value); }
	}

	public enum Stroke {
		FOREHAND_GROUNDSTROKE('f'),
		FOREHAND_SLICE ('r'),
		FOREHAND_VOLLEY ('v'),
		FOREHAND_OVERHEAD ('o'),
		FOREHAND_DROPSHOT ('u'),
		FOREHAND_LOB ('l'),
		FOREHAND_HALFVOLLEY ('h'),
		FOREHAND_SWINGINGVOLLEY ('j'),

        BACKHAND_GROUNDSTROKE ('b'),
        BACKHAND_SLICE ('s'),
		BACKHAND_VOLLEY ('z'),
        BACKHAND_OVERHEAD ('p'),
        BACKHAND_DROPSHOT ('y'),
        BACKHAND_LOB ('m'),
        BACKHAND_HALFVOLLEY ('i'),
        BACKHAND_SWINGINGVOLLEY ('k'),
		
		STROKE_TRICK ('t'),
		STROKE_UNKNOWN ('q');

		private char value;
		private Stroke(char v) { value = v; }
		public char asChar() { return value; }
		public String toString() { return Character.toString(value); }
	}

	public enum Direction {
        DEUCE ('1'), MIDDLE ('2'), AD ('3'), UNKNOWN ('0');
		private char value;
		private Direction(char v) { value = v; }
		public char asChar() { return value; }
		public String toString() { return Character.toString(value); }
	}

	public enum ServeDirection {
        WIDE ('4'), BODY ('5'), T ('6'), UNKNOWN ('0');
		private char value;
		private ServeDirection(char v) { value = v; }
		public char asChar() { return value; }
		public String toString() { return Character.toString(value); }
	}
	
	public enum Depth {
        SHORT ('4'), MID ('5'), DEEP ('6'), UNKNOWN ('0');
		private char value;
		private Depth(char v) { value = v; }
		public char asChar() { return value; }
		public String toString() { return Character.toString(value); }
	}

	public enum PointOutcome {
		WINNER('*'), ACE('*'), UNRETURNABLE('#'), 
		NET ('n'), WIDE ('w'), DEEP ('d'), WIDE_DEEP ('x'), SHANK ('!'), UNKNOWN ('e');
		private char value;
		private PointOutcome(char v) { value = v; }
		public char asChar() { return value; }
		public String toString() { return Character.toString(value); }
    }

	public enum ErrorType {
        FORCED ('#'), UNFORCED ('@');
		private char value;
		private ErrorType(char v) { value = v; }
		public char asChar() { return value; }
		public String toString() { return Character.toString(value); }
    }

	public Point(boolean firstServe, int server) {
		this(firstServe, server, "");
	}

	public Point(boolean firstServe, int server, String point) {
		mFirstServe = firstServe;
		mServer = server;
		data = new StringBuilder(point);
	}

	public void givePoint(Point.PointGiven pg) {
		data = new StringBuilder().append(pg);
		endPoint();
	}

	public void serve(Point.ServeDirection sd) {
		if (sd != null) {
			data.append(sd.asChar());
		}
	}

	public void addStroke(Point.Stroke s, Point.Direction dir) {
		addStroke(s, dir, null);
	}

	public void addStroke(Point.Stroke s, Point.Direction dir, Point.Depth d) {
		if (s == null || dir == null) {
			throw new InvalidParameterException();
		}

		data.append(s.asChar());
		data.append(dir.asChar());
		if (d != null)
			data.append(d.asChar());
	}
	
	public void endPoint() {
		endPoint(null, null);
	}

	public void endPoint(Point.PointOutcome o) {
		endPoint(o, null);
	}

	public void endPoint(Point.PointOutcome o, Point.ErrorType et) {
		if (o != null) {
			data.append(o.asChar());
			if (o != PointOutcome.WINNER && et != null)
				data.append(et.asChar());
		}
	}
	
	public int winner() {
		if (data.length() == 0 || !isValid())
			return 0;

		char first = data.charAt(0);
		if (first == PointGiven.POINT_RETURNER.asChar() || first == PointGiven.POINT_RETURNER_PENALTY.asChar()) {
			return returner();
		} else if (first == PointGiven.POINT_SERVER.asChar() || first == PointGiven.POINT_SERVER_PENALTY.asChar()) {
			return server();
		} else {
			Matcher m = ENDING_PATTERN.matcher(data);
			if (!m.find()) {
				// Point isn't over
				return 0;
			}

			int c = shotCount();
			String outcome = m.group();
			if (c == 1) {
				if (outcome.equals(PointOutcome.ACE.toString()) || 
						outcome.equals(PointOutcome.UNRETURNABLE.toString()))
					return server();
				else
					return returner();
			} else {
				if (outcome.equals(PointOutcome.WINNER.toString())) {
					return c % 2 == 1 ? server() : returner();
				} else { // Error
					return c % 2 == 1 ? returner() : server();
				}
			}
		}
	}

	public int shotCount() {
		if (data.length() < 2)
			return 0;
		else
			return SHOT_PATTERN.split(data).length;
	}

	public int server() {
		return mServer;
	}
	
	public int returner() {
		return mServer == 1 ? 2 : 1;
	}
	
	public void setPoint(String point) {
		data = new StringBuilder(point);
	}
	
	public String toString() {
		return data.toString();
	}
	
	public boolean fault() {
		return (winner() == returner() && shotCount() == 1);
	}
	
	/**
	 * Replays the point for the winner and the shot-count
	 */
	public void replayPoint() {
		if (data.length() == 0)
			return;
		
	}

	public boolean isValid() {
		return POINT_PATTERN.matcher(data).matches();
	}
	
	public boolean isFirstServe() {
		return mFirstServe;
	}
	
}