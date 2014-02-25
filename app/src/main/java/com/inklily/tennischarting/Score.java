package com.inklily.tennischarting;

import java.util.Collections;
import java.util.List;

/**
 * Scorekeeping class.
 */
public class Score {
    private final static String[] PTS_TABLE = {"0", "15", "30", "40", "Ad"};
	private int[] p1_games;
	private int[] p1_tbs;
	private int[] p2_games;
	private int[] p2_tbs;
	private int p1_pts = 0;
	private int p2_pts = 0;
	private int mSets;
	private boolean mFinalTb;
    private boolean mFirstServe = true;

	private int mCurrentSet = 0;
	
	public Score(int sets, boolean final_tb) {
		setSets(sets);
		setFinalTb(final_tb);
	}

    /**
     * Copy constructor
     * @param score
     */
    public Score(Score score) {
        this(score.mSets, score.mFinalTb);
        System.arraycopy(score.p1_games, 0, this.p1_games, 0, score.p1_games.length);
        System.arraycopy(score.p1_tbs, 0, this.p1_tbs, 0, score.p1_tbs.length);
        System.arraycopy(score.p2_games, 0, this.p2_games, 0, score.p2_games.length);
        System.arraycopy(score.p2_tbs, 0, this.p2_tbs, 0, score.p2_tbs.length);
        this.p1_pts = score.p1_pts;
        this.p2_pts = score.p2_pts;
        this.mFirstServe = score.mFirstServe;
        this.mCurrentSet = score.mCurrentSet;
    }

    public void setFinalTb(boolean t) {
		mFinalTb = t;
		reset_score();
	}

    public boolean isComplete() {
        return mCurrentSet >= mSets;
    }

	
	/**
	 * Set the number of sets and recalculate the score.
	 * @param sets
	 */
	public void setSets(int sets) {
		mSets = sets;

		p1_games = new int[sets];
		p2_games = new int[sets];
		p1_tbs = new int[sets];
		p2_tbs = new int[sets];
		reset_score();
	}

	private void reset_score() {
		int[][] arrays = { p1_games, p2_games, p1_tbs, p2_tbs };
		for (int[] a : arrays) {
			for (int i = 0; i < a.length; i++) {
				a[i] = 0;
			}
		}
		mCurrentSet = 0;
	}

    private int changes() {
        int c = 0;
        for (int i = 0; i < p1_tbs.length; i++) {
            c += (p1_games[i] + p2_games[i] + 1) / 2;
            c += (p1_tbs[i] + p2_tbs[i]) / 6;
        }
        return c;
    }

    public int games() {
		int c = 0;
		int[][] arrays = { p1_games, p2_games };
		for (int[] a : arrays) {
			for (int i = 0; i < a.length; i++) {
				c += a[i];
			}
		}
		return c;
	}
	
	/**
	 * Returns the id (1 or 2) of the server.
	 */
	public int server() {
		int server = games() % 2 + 1;
		if (in_tb()) {
			int pts = p1_pts + p2_pts;
			boolean alt = ((pts + 1) / 2) % 2 == 1;

			if (alt)
				server = server == 1 ? 2 : 1;
		}
		return server;
	}

    /**
     * Returns the id (1 or 2) of the returner.
     */
    public int returner() {
        return server() == 1 ? 2 : 1;
    }

	/**
	 * Adds a point to the score.
	 * @param p point to score
     * @return true if point was scored
	 */
	public boolean score_point(Point p) {
		if (mCurrentSet == mSets)
			return false; // Match is over

		Point.PointWinner pointOutcome = p.winner();

		if (pointOutcome == Point.PointWinner.NONE)
			return false;

        int winner;
        if (pointOutcome == Point.PointWinner.SERVER)
            winner = server();
        else
            winner = returner();

        // A first-serve fault doesn't affect the score
        if (p.isFault() && mFirstServe) {
            mFirstServe = false;
            return true;
        } else {
            mFirstServe = true;
        }

        if (winner == 1) {
			p1_pts += 1;
		} else if (winner == 2) {
			p2_pts += 1;
		}
		
		normalize_score();
        return true;
	}
	
	/**
	 * True if this is a tiebreak.
	 * @return
	 */
	public boolean in_tb() {
        if (isComplete())
            return false;

		if (p1_games[mCurrentSet] == 6
				&& p2_games[mCurrentSet] == 6
				&& !(mCurrentSet == mSets - 1 && !mFinalTb)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Makes a new set. 
	 */
	private void new_set() {
        mCurrentSet++;
	}

	/**
	 * Makes a new game.
	 * 
	 * Translate games into points, update structures for new game.
	 */
	private void new_game() {
        // Save this, it can change as we change state
        boolean was_in_tb = in_tb();

		if (p1_pts > p2_pts)
			p1_games[mCurrentSet]++;
		else if (p2_pts > p1_pts)
			p2_games[mCurrentSet]++;
		else
			return;

        if (was_in_tb) {
            p1_tbs[mCurrentSet] = p1_pts;
            p2_tbs[mCurrentSet] = p2_pts;
        }

        p1_pts = 0;
        p2_pts = 0;

        int p1_game = p1_games[mCurrentSet];
        int p2_game = p2_games[mCurrentSet];
        if (was_in_tb && (p1_game == 7 || p2_game == 7))
			new_set();
		else if ( (p1_game > 5 && p1_game - p2_game > 1) 
				|| (p2_game > 5 && p2_game - p1_game > 1))
			new_set();

    }

	/** 
	 * Translate points into games as needed.
	 */
	private void normalize_score() {
		if (in_tb()) {
			if (p1_pts > 6 && p1_pts - p2_pts > 1) {
				new_game();
			} else if (p2_pts > 6 && p2_pts - p1_pts > 1) {
				new_game();
			}
		} else {
			if (p1_pts > 3 && p1_pts - p2_pts > 1) {
				new_game();
			} else if (p2_pts > 3 && p2_pts - p1_pts > 1) {
				new_game();
			} else if (p1_pts > 4 || p2_pts > 4 || p1_pts == 4 && p2_pts == 4) {
                // Deal with deuce games
                p1_pts--;
                p2_pts--;
            }
		}
	}

	/**
	 * Recalculate score for the given points.
	 */
	public void reScore(List<Point> points) {
		reset_score();
		for (Point p : points)
			score_point(p);
	}
	
	public String toString() {
        if (isComplete()) {
            return gameScore();
        } else {
            return String.format("%s:%s", gameScore(), ptsScore());
        }
	}
	
	/**
	 * Returns true if serve is from the deuce court.
	 */
	public boolean deuceCourt() {
		return ((p1_pts + p2_pts) % 2 == 0);
	}

	/**
	 * Returns true if next receiver is near to the camera, assuming the
     * first receiver was far from the camera.
	 */
	public boolean near() {
        int c = changes();
        if (in_tb())
            c += (p1_pts + p2_pts) / 6;
		int nearPlayer = c % 2 + 1;
        return nearPlayer != server();
	}

    public String gameScore() {
        StringBuilder score = new StringBuilder();
        int end = mCurrentSet == mSets ? mSets : mCurrentSet + 1;
        for (int i = 0; i < end; i++) {
            int t1 = p1_tbs[i];
            int t2 = p2_tbs[i];
            if (t1 != 0 || t2 != 0)
                score.append(String.format("%s-%s(%s)", p1_games[i], p2_games[i], t1 < t2 ? t1 : t2));
            else
                score.append(String.format("%s-%s", p1_games[i], p2_games[i]));
            if (i != end - 1)
                score.append(" ");
        }


        return score.toString();
    }

    public String ptsScore() {
        if (!isComplete()) {
            String p1_pts_str;
            String p2_pts_str;
            if (in_tb()) {
                p1_pts_str = Integer.toString(p1_pts);
                p2_pts_str = Integer.toString(p2_pts);
            } else {
                p1_pts_str = PTS_TABLE[p1_pts];
                p2_pts_str = PTS_TABLE[p2_pts];
            }
            if (server() == 1)
                return (String.format("%s-%s", p1_pts_str, p2_pts_str));
            else
                return (String.format("%s-%s", p2_pts_str, p1_pts_str));
        } else {
            return "";
        }
    }

    public boolean isFirstServe() {
        return mFirstServe;
    }


    public boolean isNewGame() {
        return !isComplete() && p1_pts == 0 && p2_pts == 0;
    }

    public boolean isNewSet() {
        return isNewGame() && !isComplete() && p1_games[mCurrentSet] == 0 && p2_games[mCurrentSet] == 0;
    }
}
