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
	public int sets;
	public boolean final_tb;
	public String charted_by;
	
	public Match() {
		points = new ArrayList<Point>();
	}

	void addPoint(Point p) {
		points.add(p);
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

		output.append(",").append(sets).append('\n');
		output.append(",").append(final_tb ? "1" : "0").append('\n');
		output.append(",").append(sets).append('\n');
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
