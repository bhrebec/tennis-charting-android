package com.inklily.tennischarting;

import java.util.List;

/**
 * Scorekeeping class.
 */
public class Score {
    private final static String[] PTS_TABLE = {"0", "15", "30", "40", "Ad"};
	private int[] mP1Games;
	private int[] mP1Tbs;
	private int[] mP2Games;
	private int[] mP2Tbs;
	private int mP1Pts = 0;
	private int mP2Pts = 0;
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
        System.arraycopy(score.mP1Games, 0, this.mP1Games, 0, score.mP1Games.length);
        System.arraycopy(score.mP1Tbs, 0, this.mP1Tbs, 0, score.mP1Tbs.length);
        System.arraycopy(score.mP2Games, 0, this.mP2Games, 0, score.mP2Games.length);
        System.arraycopy(score.mP2Tbs, 0, this.mP2Tbs, 0, score.mP2Tbs.length);
        this.mP1Pts = score.mP1Pts;
        this.mP2Pts = score.mP2Pts;
        this.mFirstServe = score.mFirstServe;
        this.mCurrentSet = score.mCurrentSet;
    }

    public void setFinalTb(boolean t) {
		mFinalTb = t;
		reset_score();
	}

    public int winner() {
        int sets_p1 = 0;
        int sets_p2 = 0;
        for (int i = 0; i < mCurrentSet; i++) {
            if (mP1Games[i] > mP2Games[i])
                sets_p1++;
            else
                sets_p2++;
        }

        if (sets_p1 > mSets / 2)
            return 1;
        else if (sets_p2 > mSets / 2)
            return 2;
        else
            return 0;
    }

    public boolean isComplete() {
        return mCurrentSet >= mSets || winner() != 0;
    }

	
	/**
	 * Set the number of sets and recalculate the score.
	 * @param sets
	 */
	public void setSets(int sets) {
		mSets = sets;

		mP1Games = new int[sets];
		mP2Games = new int[sets];
		mP1Tbs = new int[sets];
		mP2Tbs = new int[sets];
		reset_score();
	}

	private void reset_score() {
		int[][] arrays = {mP1Games, mP2Games, mP1Tbs, mP2Tbs};
		for (int[] a : arrays) {
			for (int i = 0; i < a.length; i++) {
				a[i] = 0;
			}
		}
		mCurrentSet = 0;
	}

    private int changes() {
        int c = 0;
        for (int i = 0; i < mP1Tbs.length; i++) {
            c += (mP1Games[i] + mP2Games[i] + 1) / 2;
            c += (mP1Tbs[i] + mP2Tbs[i]) / 6;
        }
        return c;
    }

    public int games() {
		int c = 0;
		int[][] arrays = {mP1Games, mP2Games};
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
			int pts = mP1Pts + mP2Pts;
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
			mP1Pts += 1;
		} else if (winner == 2) {
			mP2Pts += 1;
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

		if (mP1Games[mCurrentSet] == 6
				&& mP2Games[mCurrentSet] == 6
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

		if (mP1Pts > mP2Pts)
			mP1Games[mCurrentSet]++;
		else if (mP2Pts > mP1Pts)
			mP2Games[mCurrentSet]++;
		else
			return;

        if (was_in_tb) {
            mP1Tbs[mCurrentSet] = mP1Pts;
            mP2Tbs[mCurrentSet] = mP2Pts;
        }

        mP1Pts = 0;
        mP2Pts = 0;

        int p1_game = mP1Games[mCurrentSet];
        int p2_game = mP2Games[mCurrentSet];
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
			if (mP1Pts > 6 && mP1Pts - mP2Pts > 1) {
				new_game();
			} else if (mP2Pts > 6 && mP2Pts - mP1Pts > 1) {
				new_game();
			}
		} else {
			if (mP1Pts > 3 && mP1Pts - mP2Pts > 1) {
				new_game();
			} else if (mP2Pts > 3 && mP2Pts - mP1Pts > 1) {
				new_game();
			} else if (mP1Pts > 4 || mP2Pts > 4 || mP1Pts == 4 && mP2Pts == 4) {
                // Deal with deuce games
                mP1Pts--;
                mP2Pts--;
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
		return ((mP1Pts + mP2Pts) % 2 == 0);
	}

	/**
	 * Returns true if next receiver is near to the camera, assuming the
     * first receiver was far from the camera.
	 */
	public boolean near() {
        int c = changes();
        if (in_tb())
            c += (mP1Pts + mP2Pts) / 6;
		int nearPlayer = c % 2 + 1;
        return nearPlayer != server();
	}

    public String gameScore() {
        StringBuilder score = new StringBuilder();
        int end = mCurrentSet == mSets ? mSets : mCurrentSet + 1;
        for (int i = 0; i < end; i++) {
            int t1 = mP1Tbs[i];
            int t2 = mP2Tbs[i];
            if (t1 != 0 || t2 != 0)
                score.append(String.format("%s-%s(%s)", mP1Games[i], mP2Games[i], t1 < t2 ? t1 : t2));
            else
                score.append(String.format("%s-%s", mP1Games[i], mP2Games[i]));
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
                p1_pts_str = Integer.toString(mP1Pts);
                p2_pts_str = Integer.toString(mP2Pts);
            } else {
                p1_pts_str = PTS_TABLE[mP1Pts];
                p2_pts_str = PTS_TABLE[mP2Pts];
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
        return !isComplete() && mP1Pts == 0 && mP2Pts == 0;
    }

    public boolean isNewSet() {
        return isNewGame() && !isComplete() && mP1Games[mCurrentSet] == 0 && mP2Games[mCurrentSet] == 0;
    }
}
