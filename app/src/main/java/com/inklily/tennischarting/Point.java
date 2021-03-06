package com.inklily.tennischarting;

import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single point in chart form.
 *
 * TODO: the 'seq' item is pretty opaque/problematic. Fix it.
 */
public class Point {
	public Integer seq;
	private StringBuilder mData;

	private String mComments = "";
	
	// TODO: build these from the enums?
	private static final String VALID_SHOTS = "frvoulhjbszpymiktq";
	private static final String SERVE_PATTERN_STRING = "^c*[0456][+]?";
	private static final String RALLY_PATTERN_STRING = "[" + VALID_SHOTS + "][=+;-]?[0123]?[0789]?";
	private static final String ENDING_PATTERN_STRING = "([*e#@nwdxg!]|[nwdx!e][#@])$";
    private static final Pattern SERVE_PATTERN = Pattern.compile(SERVE_PATTERN_STRING);
	private static final Pattern ENDING_PATTERN = Pattern.compile(ENDING_PATTERN_STRING);
	private static final Pattern SHOT_PATTERN = Pattern.compile("[" + VALID_SHOTS + "]");
	private static final Pattern POINT_PATTERN = Pattern.compile("P|Q|R|S|" + SERVE_PATTERN_STRING 
			+ "(" + RALLY_PATTERN_STRING + ")*" + ENDING_PATTERN_STRING);

    public String getComments() {
        if (mComments == null)
            return "";
        else
            return mComments;
    }

    public void setComments(String comments) {
        mComments = comments;
    }

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

    public enum Approach {
        SERVE_AND_VOLLEY('+'),
        NET_APPROACH('+');

        private char value;
        private Approach(char v) { value = v; }
        public char asChar() { return value; }
        public String toString() { return Character.toString(value); }
    }
	public enum NetPosition {
		AT_NET('-'),
		AT_BASELINE('=');

		private char value;
		private NetPosition(char v) { value = v; }
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
        SHORT ('7'), MID ('8'), DEEP ('9'), UNKNOWN ('0');
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

    public enum PointWinner { NONE, SERVER, RETURNER }

	/**
	 * Copy constructor
	 */
	public Point(Point p) {
		this(p.toString());
		this.seq = p.seq;
		this.setComments(p.getComments());
	}

	/**
	 * Create an empty point.
	 * 
	 */
	public Point() {
		this("");
	}

	/**
	 * 
	 * @param point string representation of the point
	 */
	public Point(String point) {
		mData = new StringBuilder(point);
	}

	public void givePoint(Point.PointGiven pg) {
		mData = new StringBuilder().append(pg);
	}

    public void serve(Point.ServeDirection sd) {
        serve(sd, null);
    }

    public void serve(Point.ServeDirection sd, Approach np) {
        if (sd != null) {
            mData.append(sd.asChar());
            if (np != null) {
                mData.append(np.asChar());
            }
        }
    }

	/**
	 * Adds a letchord. Will destroy any non-letchord strokes in the point.
	 */
	public void addLetchord() {
		mData = new StringBuilder(mData.toString().replaceAll("[^c]", ""));
		mData.append(Stroke.LETCORD.asChar());
	}

	public void addStroke(Point.Stroke s) {
		addStroke(s, null, null, null, null);
	}

    public void addStroke(Point.Stroke s, Point.Direction dir) {
        addStroke(s, dir, null, null, null);
    }

    public void addStroke(Point.Stroke s, Point.Direction dir, Point.Depth d) {
        addStroke(s, dir, d, null, null);
    }

    public void addStroke(Point.Stroke s, Point.Direction dir, Point.Depth d, Approach approach) {
        addStroke(s, dir, d, approach, null);
    }

	public void addStroke(Point.Stroke s, Point.Direction dir, Point.Depth d, Approach approach, NetPosition np) {
		if (s == null) {
			throw new InvalidParameterException();
		}

		reopenPoint();
		mData.append(s.asChar());
        if (approach != null)
            mData.append(approach.asChar());
        if (np != null)
            mData.append(np.asChar());
        if (dir != null)
            mData.append(dir.asChar());
		if (d != null)
			mData.append(d.asChar());
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
		Matcher m = ENDING_PATTERN.matcher(mData);
		while (m.find())
			mData.delete(m.start(), m.end());

		if (o != null) {
			mData.append(o.asChar());
			if (o != PointOutcome.WINNER && et != null)
				mData.append(et.asChar());
		} else if (et != null) {
			mData.append(et.asChar());
		}
	}

    /**
     * @return 1 if the server won the point, 2 if the returner, 0 if the point isn't over
     */
	public PointWinner winner() {
		if (mData.length() == 0 || !isValid())
			return PointWinner.NONE;

		char first = mData.charAt(0);
		if (first == PointGiven.POINT_RETURNER.asChar() || first == PointGiven.POINT_RETURNER_PENALTY.asChar()) {
			return PointWinner.RETURNER;
		} else if (first == PointGiven.POINT_SERVER.asChar() || first == PointGiven.POINT_SERVER_PENALTY.asChar()) {
			return PointWinner.SERVER;
		} else {
			Matcher m = ENDING_PATTERN.matcher(mData);
			if (!m.find()) {
				// Point isn't over
				return PointWinner.NONE;
			}

			int c = shotCount();
			String outcome = m.group();
			if (c == 1) {
				if (outcome.equals(PointOutcome.ACE.toString()) || 
						outcome.equals(PointOutcome.UNRETURNABLE.toString()))
                    return PointWinner.SERVER;
				else
                    return PointWinner.RETURNER;
			} else {
				if (outcome.equals(PointOutcome.WINNER.toString())) {
					return c % 2 == 1 ? PointWinner.SERVER : PointWinner.RETURNER;
				} else { // Force or unforces error
					return c % 2 == 1 ?  PointWinner.RETURNER : PointWinner.SERVER;
				}
			}
		}
	}

	public int shotCount() {
		// Remove letchords,  they mess up the count
		String clean_data = mData.toString().replace("c", "");
		if (clean_data.length() == 0
                || !SERVE_PATTERN.matcher(clean_data).find())
			return 0;
		else
			return SHOT_PATTERN.split(clean_data + " ").length; // Add a trailing space to correct points ending with a stroke
	}

	public void setPoint(String point) {
		mData = new StringBuilder(point);
	}
	
	public String toString() {
		return mData.toString();
	}
	
	public boolean isFault() {
		return (winner() == PointWinner.RETURNER && shotCount() == 1);
	}
	
	public boolean isValid() {
		return POINT_PATTERN.matcher(mData).matches();
	}

    /**
     * @return id of next player to strike the ball
     */
	public int nextPlayer() {
		return (shotCount() % 2) == 0 ? 1 : 2;
	}
}