package com.inklily.tennischarting;

import java.util.ArrayList;
import java.util.List;

public class Match {
	public Long id = null;
	
	public List<Point> points;
	
	public String player1;
	public String player2;
	public char player1hand;
	public char player2hand;
	public char gender;
	public String date;
	public String tournament;
	public String round;
	public String time;
	public String court;
	public String surface;
	public String umpire;
	private int mSets;
	private boolean mFinalTb;
	public String charted_by;
    public boolean sent;

	public boolean nearServerFirst; // true if first server is closest to the camera
	
	// Cache of the latest score
	private Score current_score;

    public Match(int sets, boolean final_tb, boolean nearServerFirst) {
		this.nearServerFirst = nearServerFirst;
		this.mSets = sets;
		this.mFinalTb = final_tb;
		current_score = new Score(sets, final_tb);
		points = new ArrayList<Point>();
	}
	
	public boolean finalTb() {
		return mFinalTb;
	}

    /**
     * @return true if next return stroke will be near the camera
     */
	public boolean near() {
		return current_score.near() ^ (!nearServerFirst);
	}
	
	public boolean deuceCourt() {
		return current_score.deuceCourt();
	}
	
	public int sets() {
		return mSets;
	}

	public int server() {
		return current_score.server();
	}

	public void setSets(int sets) {
		current_score.setSets(sets);
		current_score.reScore(points);
	}

	public void setFinalTb(boolean ftb) {
		mFinalTb = ftb;
		current_score.setFinalTb(ftb);
		current_score.reScore(points);
	}

	public boolean isComplete() {
		return current_score.isComplete();
	}

	public boolean rightHanded(int player) {
		if (player == 1)
			return player1hand == 'R';
		else
			return player2hand == 'R';
	}
	public void addPoint(Point p) {
		p.seq = points.size();
		points.add(p);
		current_score.score_point(p);
	}

    public void addPoint(Point p, MatchStorage storage) throws MatchStorage.MatchStorageNotAvailableException {
        addPoint(p);
        storage.savePoint(this, p);
    }
	public String getScoreBefore(int before) {
		Score s = new Score(mSets, mFinalTb);
		s.reScore(points.subList(0, before));
		return s.toString();
	}
	
	public Score score() {
		return current_score;
	}

    public void rescore() {
        current_score.reScore(points);
    }

    /**
     * return True if next stroke will be right-handed for the given in-progress point.
     * @param inProgressPoint
     */
    public boolean nextStrokeRighthandedFor(Point inProgressPoint) {
        int n = inProgressPoint.nextPlayer();

        // Handedness of the next shot striker
        return rightHanded(n % 2 + 1);
    }

    /**
     * return True if next location will be right-handed for the given in-progress point.
     * @param inProgressPoint
     */
    public boolean nextLocRighthandedFor(Point inProgressPoint) {
        int n = inProgressPoint.nextPlayer();

        // Handedness of the next shot returner
        return rightHanded(n);
    }

    /**
     * return True if next stroke will be near for the given in-progress point.
     * @param inProgressPoint
     */
    public boolean nextNearFor(Point inProgressPoint) {
        return near() ^ (inProgressPoint.shotCount() % 2 == 0);
    }

    String outputRow(Point serve1, Point serve2) {
		if (serve1 != null) {
			// Output row
			String comments = serve1.comments + (serve2 == null ? "" : serve2.comments);
			return String.format(",,,,,,,,%s,%s,%s\n", serve1, serve2 == null ? "" : serve2, comments);
		}
		return "";
	}
	
	String outputSpreadsheet() {
		StringBuilder output = new StringBuilder();
		String[] players = { player1, player2 };
		for (String player : players) {
			output.append(",").append(player).append('\n');
		}
		
		char[] player_info = { player1hand, player2hand, gender };
		for (char info : player_info) {
			output.append(",").append(info).append('\n');
		}
		
		String[] match_info = { date, tournament, round, time, court, surface, umpire};
		for (String info : match_info) {
			output.append(",").append(info).append('\n');
		}

		output.append(",").append(sets()).append('\n');
		output.append(",").append(mFinalTb ? "1" : "0").append('\n');
		output.append(",").append(mSets).append('\n');
		output.append(",\n,\n");
		Point serve1 = null;
		Point serve2 = null;

        // Score along to determine first serves
        Score tmpScore = new Score(mSets, mFinalTb);
		for (Point p : points) {
			if (tmpScore.isFirstServe()) {
				output.append(outputRow(serve1, serve2));
				serve1 = p;
				serve2 = null;
			} else {
				serve2 = p;
			}
            tmpScore.score_point(p);
		}
		output.append(outputRow(serve1, serve2)); // output the last row if there is one
		
		return output.toString();
	}

    public String playerName(int i) {
        if (i == 1)
            return player1;
        else
            return player2;
    }
}
