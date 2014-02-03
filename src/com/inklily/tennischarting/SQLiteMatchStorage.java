package com.inklily.tennischarting;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

public class SQLiteMatchStorage implements MatchStorage {
	private final static String DATABASE_NAME = "matches";
	private final static int DB_VERSION_1 = 1;
	private final static int CURRENT_DB_VERSION = DB_VERSION_1;
	
	private static SQLiteMatchStorage _instance = null;

	public static SQLiteMatchStorage getGlobalInstance(Context cxt) {
		if (_instance == null) {
			_instance = new SQLiteMatchStorage(cxt.getApplicationContext());
		}
		return _instance;
	}
	
	public static void closeGlobalInstance() {
		if (_instance != null)
			_instance.close();
	}
	
	private static class MatchSQLHelper extends SQLiteOpenHelper {
		private MatchSQLHelper(Context context, String db_name) {
			super(context, db_name, null, CURRENT_DB_VERSION);
		}
		
		public static MatchSQLHelper makeHelper(Context context) {
			File exdir = context.getExternalFilesDir(null);
			if (exdir == null)
				return new MatchSQLHelper(context, DATABASE_NAME);
			else
				return new MatchSQLHelper(context, exdir + File.separator + DATABASE_NAME);
				
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE match(id INTEGER, " +
					"player1, player2 , player1hand , player2hand , " +
					"date, tournament, round, time, court, surface, " +
					"umpire, sets, final_tb, charted_by, complete, near, " +
					"PRIMARY KEY (id))");
			db.execSQL("CREATE TABLE point(match_id INTEGER, " +
                    "seq INTEGER, point TEXT, first_serve, server, UNIQUE(match_id, seq))");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {			
		}
	}
	
	private class DBOpenAsync extends AsyncTask<SQLiteOpenHelper, Void, SQLiteDatabase> {
		boolean failure = false;

		@Override
		protected SQLiteDatabase doInBackground(SQLiteOpenHelper... helpers) {
			if (helpers.length == 0)
				return null;
			try {
				return helpers[0].getWritableDatabase();
			} catch (SQLiteException ex) {
				ex.printStackTrace();
				failure = true;
				return null;
			}
		}
		
	    protected void onPostExecute(SQLiteDatabase result) {
	    	if (failure)
	    		;
	        db = result;
	    }
	}

	private MatchSQLHelper helper;
	private SQLiteDatabase db;
	
	public SQLiteMatchStorage(Context cxt) {
		helper = MatchSQLHelper.makeHelper(cxt);
		//new DBOpenAsync().execute(helper);
		db = helper.getWritableDatabase();
	}

	public void close() {
		if (db != null)
			db.close();
		db = null;
	}
	
	@Override
	public void savePoint(Match m, Point p) throws MatchStorageNotAvailableException {
		if (db == null)
			throw new MatchStorageNotAvailableException();
		if (p.seq == null)
			throw new InvalidPointException();
		
		if (m.id == null)
			saveMatch(m);
		
		ContentValues vals = new ContentValues();
		vals.put("match_id", m.id);
		vals.put("seq", p.seq);
		vals.put("first_serve", p.isFirstServe());
		vals.put("server", p.server());
		vals.put("point", p.toString());
	}

	@Override
	public void saveMatch(Match m) throws MatchStorageNotAvailableException {
		if (db == null)
			throw new MatchStorageNotAvailableException();
		ContentValues vals = new ContentValues();
		vals.put("player1", m.player1);
		vals.put("player2", m.player2);
		vals.put("player1hand", Character.toString(m.player1hand));
		vals.put("player2hand", Character.toString(m.player2hand));
		vals.put("date", m.date);
		vals.put("tournament", m.tournament);
		vals.put("round", m.round);
		vals.put("time", m.time);
		vals.put("court", m.court);
		vals.put("surface", m.surface);
		vals.put("umpire", m.umpire);
		vals.put("sets", m.sets());
		vals.put("final_tb", m.finalTb());
		vals.put("charted_by", m.charted_by);
		vals.put("complete", m.isComplete());
		vals.put("near", m.nearServerFirst);
		
		if (m.id != null)
			vals.put("id", m.id);
		
		m.id = db.replace("match", null, vals);
	}

	public int getIncompleteMatchCount() throws MatchStorageNotAvailableException {
		if (db == null)
			throw new MatchStorageNotAvailableException();
		Cursor c = db.rawQuery("SELECT count(*) FROM match WHERE complete = 0", null);
		
		c.moveToFirst();
		return c.getInt(0);
	}
	
	public Match retrieveMatch(long id) throws MatchStorageNotAvailableException {
		if (db == null)
			throw new MatchStorageNotAvailableException();
		
		final String[] cols = { "id", "player1", "player2" , "player1hand" , 
			"player2hand" ,	"date", "tournament", "round", 
			"time", "court", "surface",	"umpire", 
			"sets", "final_tb", "charted_by", "complete", "near" };
		final String[] args = { Long.toString(id), };
		
		Cursor c = db.query("match", cols, "id = ?", args, null, null, null);
		if (!c.moveToFirst())
			return null;
		
		Match m = new Match(c.getInt(12), c.getInt(13) == 1, c.getInt(16) == 1);
		
        m.player1 = c.getString(1);
        m.player2 = c.getString(2);
        m.player1hand = c.getString(3).charAt(0);
        m.player2hand = c.getString(4).charAt(0);
        m.date = c.getString(5);
        m.tournament = c.getString(6);
        m.round = c.getString(7);
        m.time = c.getString(8);
        m.court = c.getString(9);
        m.surface = c.getString(10);
        m.umpire = c.getString(11);
        m.charted_by = c.getString(14);
         
 		final String[] point_cols = { "match_id", "first_serve", "server", "seq", "point" };
 		Cursor pc = db.query("point", point_cols, "match_id = ?", args, null, null, null);
 		while (pc.moveToNext()) {
 			Point p = new Point(pc.getInt(1) == 1, pc.getInt(2), pc.getString(4));
 			p.seq = pc.getInt(3);
 			m.addPoint(p);
 		}
 		 
        return m;
	}
	
	
}
