package com.inklily.tennischarting;

import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Point {
	public Integer seq;
	private StringBuilder data;

	private boolean mFirstServe;
	private int mServer;
	public String comments;
	
	// TODO: build these from the enums?
	private static final String VALID_SHOTS = "frvoulhjbszpymiktq";
	private static final String SERVE_PATTERN_STRING = "c*^[0456]";
	private static final String RALLY_PATTERN_STRING = "[" + VALID_SHOTS + "][0123]?[0789]?";
	private static final String ENDING_PATTERN_STRING = "([*e#@nwdxg!]|[nwdx!e][#@])$";
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
		
		TRICK ('t'),
		UNKNOWN ('q'),
		LETCORD ('c'),
		NO_STROKE (' ');

		private char value;
		private Stroke(char v) { value = v; }
		public char asChar() { return value; }
		public String toString() { return Character.toString(value); }
	}
	
	public abstract static class StrokeIndex {
		public Stroke GROUNDSTROKE = Stroke.NO_STROKE;
		public Stroke SLICE = Stroke.NO_STROKE;
		public Stroke VOLLEY = Stroke.NO_STROKE;
		public Stroke OVERHEAD = Stroke.NO_STROKE;
		public Stroke LOB = Stroke.NO_STROKE;
		public Stroke DROPSHOT = Stroke.NO_STROKE;
		public Stroke HALFVOLLEY = Stroke.NO_STROKE;
		public Stroke SWINGINGVOLLEY = Stroke.NO_STROKE;
	}

	public final static StrokeIndex FOREHAND_STROKES = new StrokeIndex() {
		{
		GROUNDSTROKE = Stroke.FOREHAND_GROUNDSTROKE;
		SLICE = Stroke.FOREHAND_SLICE; 
		VOLLEY = Stroke.FOREHAND_VOLLEY; 
		OVERHEAD = Stroke.FOREHAND_OVERHEAD; 
		DROPSHOT = Stroke.FOREHAND_DROPSHOT; 
		HALFVOLLEY = Stroke.FOREHAND_HALFVOLLEY; 
		SWINGINGVOLLEY = Stroke.FOREHAND_SWINGINGVOLLEY; 
		LOB = Stroke.FOREHAND_LOB; 
		}
	};

	public final static StrokeIndex BACKHAND_STROKES = new StrokeIndex() {
		{
		GROUNDSTROKE = Stroke.BACKHAND_GROUNDSTROKE;
		SLICE = Stroke.BACKHAND_SLICE; 
		VOLLEY = Stroke.BACKHAND_VOLLEY; 
		OVERHEAD = Stroke.BACKHAND_OVERHEAD; 
		DROPSHOT = Stroke.BACKHAND_DROPSHOT; 
		HALFVOLLEY = Stroke.BACKHAND_HALFVOLLEY; 
		SWINGINGVOLLEY = Stroke.BACKHAND_SWINGINGVOLLEY; 
		LOB = Stroke.BACKHAND_LOB; 
		}
	};

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
		NET ('n'), WIDE ('w'), DEEP ('d'), WIDE_DEEP ('x'), SHANK ('!'), UNKNOWN ('e'),
		FOOT_FAULT ('g');
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

	/**
	 * Copy constructor
	 */
	public Point(Point p) {
		this(p.mFirstServe, p.mServer, p.toString());
		this.seq = p.seq;
		this.comments = p.comments;
	}

	/**
	 * Create an empty point.
	 * 
	 * @param firstServe True if this point is a first serve
	 * @param server 1 or 2, the player who is serving
	 */
	public Point(boolean firstServe, int server) {
		this(firstServe, server, "");
	}

	/**
	 * 
	 * @param firstServe True if this point is a first serve
	 * @param server 1 or 2, the player who is serving
	 * @param point string representation of the point
	 */
	public Point(boolean firstServe, int server, String point) {
		mFirstServe = firstServe;
		mServer = server;
		data = new StringBuilder(point);
	}

	public void givePoint(Point.PointGiven pg) {
		data = new StringBuilder().append(pg);
	}

	public void serve(Point.ServeDirection sd) {
		if (sd != null) {
			data.append(sd.asChar());
		}
	}

	/**
	 * Adds a letchord. Will destroy any non-letchord strokes in the point.
	 */
	public void addLetchord() {
		data = new StringBuilder(data.toString().replaceAll("[^c]", ""));
		data.append(Stroke.LETCORD.asChar());
	}

	public void addStroke(Point.Stroke s) {
		addStroke(s, null, null);
	}

	public void addStroke(Point.Stroke s, Point.Direction dir) {
		addStroke(s, dir, null);
	}

	public void addStroke(Point.Stroke s, Point.Direction dir, Point.Depth d) {
		if (s == null) {
			throw new InvalidParameterException();
		}

		reopenPoint();
		data.append(s.asChar());
		if (dir != null)
			data.append(dir.asChar());
		if (d != null)
			data.append(d.asChar());
	}
	
	/**
	 * Remove point end
	 */
	public void reopenPoint() {
		endPoint(null, null);
	}

	public void endPoint(Point.PointOutcome o) {
		endPoint(o, null);
	}

	public void endPoint(Point.ErrorType o) {
		endPoint(null, o);
	}

	public void endPoint(Point.PointOutcome o, Point.ErrorType et) {
		// Remove any existing point end
		Matcher m = ENDING_PATTERN.matcher(data);
		while (m.find())
			data.delete(m.start(), m.end());

		if (o != null) {
			data.append(o.asChar());
			if (o != PointOutcome.WINNER && et != null)
				data.append(et.asChar());
		} else if (et != null) {
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
		// Remove letchords,  they mess up the count
		String clean_data = data.toString().replace("c", "");
		if (clean_data.length() < 1)
			return 0;
		else
			return SHOT_PATTERN.split(clean_data + " ").length; // Add a trailing space to correct points ending with a stroke
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
	
	public boolean isFault() {
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
	
	public int nextPlayer() {
		return (shotCount() % 2) == 0 ? server() : returner();
	}
}