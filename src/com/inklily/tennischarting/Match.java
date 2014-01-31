package com.inklily.tennischarting;

import java.util.ArrayList;
import java.util.List;

public class Match {
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
	
	public static class Score {
		private int[] p1_games;
		private int[] p1_tbs;
		private int[] p2_games;
		private int[] p2_tbs;
		private int p1_pts = 0;
		private int p2_pts = 0;
		private int mSets;
		private boolean mFinalTb;

		private int mCurrentSet = 0;
		
		public Score(int sets, boolean final_tb) {
			setSets(sets);
			setFinalTb(final_tb);
		}
		
		public void setFinalTb(boolean t) {
			mFinalTb = t;
			reset_score();
		}
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
		
		private int games() {
			int c = 0;
			int[][] arrays = { p1_games, p2_games };
			for (int[] a : arrays) {
				for (int i = 0; i < a.length; i++) {
					c++;
				}
			}
			return c;
		}
		private int server() {
			int server = games() % 2 + 1;
			if (in_tb()) {
				int pts = p1_pts + p2_pts;
				boolean alt = ((pts + 1) / 2) % 2 == 1;

				if (alt)
					server = server == 1 ? 2 : 1;
			}
			return server;
		}
		
		private void score_point(Point p) {
			if (mCurrentSet == mSets)
				return; // Match is over

			int winner = p.winner();
			if (winner == 0)
				return;

			if (winner == 1) {
				p1_pts += 1;
			} else if (winner == 2) {
				p2_pts += 1;
			}
			
			normalize_score();
		}
		
		private boolean in_tb() {
			if (p1_games[mCurrentSet] == 6 
					&& p2_games[mCurrentSet] == 6
					&& !(mCurrentSet == mSets - 1 && !mFinalTb)) {
				return true;
			} else {
				return false;
			}
		}

		private void new_set() {
			int p1_game = p1_games[mCurrentSet];
			int p2_game = p2_games[mCurrentSet];
			if (p1_game > p2_game || p2_game > p1_game) {
				mCurrentSet++;
			}
		}

		private void new_game() {
			if (p1_pts > p2_pts)
				p1_games[mCurrentSet]++;
			else if (p2_pts > p1_pts)
				p2_games[mCurrentSet]++;
			else
				return;

			p1_pts = 0;
			p2_pts = 0;
			
			int p1_game = p1_games[mCurrentSet];
			int p2_game = p2_games[mCurrentSet];
			
			if (in_tb() && (p1_game == 7 || p2_game == 7))
				new_set();
			else if ( (p1_game > 5 && p1_game - p2_game > 1) 
					|| (p2_game > 5 && p2_game - p1_game > 1))
				new_set();
		}

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
				}
			}
		}
	
		public void reScore(List<Point> points) {
			reset_score();
			for (Point p : points)
				score_point(p);
		}
		
		public String toString() {
			StringBuilder score = new StringBuilder();
			int end = mCurrentSet == mSets ? mSets : mCurrentSet + 1;
			for (int i = 0; i < end; i++) {
				int t1 = p1_tbs[i];
				int t2 = p2_tbs[i];
				if (t1 != 0 || t2 != 0)
					score.append(String.format("%s-%s(%s) ", p1_games[i], p2_games[i], t1 < t2 ? t1 : t2));
				else
					score.append(String.format("%s-%s ", p1_games[i], p2_games[i], t1 < t2 ? t1 : t2));
			}

			if (server() == 0)
				score.append(String.format("%s-%s ", p1_pts, p2_pts));
			else
				score.append(String.format("%s-%s ", p2_pts, p1_pts));
			return score.toString();
		}
		
		public boolean deuceCourt() {
			return ((p1_pts + p2_pts) % 2 == 0);
		}

		public boolean near() {
			boolean near = (((games() + 1) / 2) % 2) == 0;
			if (in_tb() && (((p1_pts + p2_pts) / 6) % 2 == 1))
				return !near;
			else
				return near;
		}
	}
	// Cache of the latest score
	private Score current_score;
	public boolean nearSwitch;
	
	public Match(int sets, boolean final_tb, boolean nearSwitch) {
		this.nearSwitch = nearSwitch;
		this.mSets = sets;
		this.mFinalTb = final_tb;
		current_score = new Score(sets, final_tb);
		points = new ArrayList<Point>();
	}
	
	public boolean near() {
		return current_score.near() ^ nearSwitch;
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


	public boolean rightHanded(int player) {
		if (player == 1)
			return player1hand == 'R';
		else
			return player2hand == 'R';
	}
	public void addPoint(Point p) {
		points.add(p);
		current_score.score_point(p);
	}
	
	public String score(int before) {
		Score s = new Score(mSets, mFinalTb);
		s.reScore(points.subList(0, before));
		return s.toString();
	}
	
	public String score() {
		return current_score.toString();
	}
	
	String outputRow(Point serve1, Point serve2) {
		if (serve1 != null) {
			// Output row
			String comments = serve1.comments + serve2 == null ? "" : serve2.comments;
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
		output.append("\n\n");
		Point serve1 = null;
		Point serve2 = null;
		for (Point p : points) {
			if (p.isFirstServe()) {
				output.append(outputRow(serve1, serve2));
				serve1 = p;
				serve2 = null;
			} else {
				serve2 = p;
			}
		}
		output.append(outputRow(serve1, serve2)); // output the last row if there is one
		
		return output.toString();
	}
}
