package com.inklily.tennischarting;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * SQLite implementation of match storage.
 */
public class SQLiteMatchStorage extends BaseAdapter implements MatchStorage {
    private final static String DATABASE_NAME = "matches";
	private final static int DB_VERSION_1 = 1;
    private final static int DB_VERSION_2 = 2;
    private final static int DB_VERSION_3 = 3;
	private final static int CURRENT_DB_VERSION = DB_VERSION_3;
    private final static SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private final static String[] MATCH_COLS = { "_id", "player1", "player2" , "player1hand" ,
            "player2hand" ,	"date", "tournament", "round",
            "time", "court", "surface",	"umpire",
            "sets", "final_tb", "charted_by", "complete",
            "near", "sent", "gender" };
    private final Context mContext;

    private MatchSQLHelper mHelper;
    private SQLiteDatabase mDb;
    private List<OnStorageAvailableListener> mAvailableListeners;
    private CursorAdapter mCursorAdapter;

    private DataSetObserver mCursorObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            SQLiteMatchStorage.this.notifyDataSetInvalidated();
        }
    };

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
			db.execSQL("CREATE TABLE IF NOT EXISTS match(_id INTEGER, " +
					"player1, player2 , player1hand , player2hand , " +
					"date, tournament, round, time, court, surface, " +
					"umpire, sets, final_tb, charted_by, complete, near, " +
                    "sent DEFAULT (0), date_entered, " +
					"PRIMARY KEY (_id))");
			db.execSQL("CREATE TABLE IF NOT EXISTS point(match_id INTEGER, " +
                    "seq INTEGER, point TEXT, UNIQUE(match_id, seq))");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == DB_VERSION_1)
                db.execSQL("ALTER TABLE match ADD COLUMN gender");
            if (oldVersion == DB_VERSION_2)
                db.execSQL("ALTER TABLE point ADD COLUMN comments");
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
	    	if (failure) {
                // TODO: make a listener for this
            } else {
                mDb = result;
                mCursorAdapter = matchCursorAdapterFactory();
                mCursorAdapter.registerDataSetObserver(mCursorObserver);
                Log.d("SQLMatchStorage", "Storage Available");
                for (OnStorageAvailableListener l : mAvailableListeners)
                    l.onStorageAvailable(SQLiteMatchStorage.this);
            }
	    }
	}

	public SQLiteMatchStorage(Context cxt) {
        mAvailableListeners = new ArrayList<OnStorageAvailableListener>();
		mHelper = MatchSQLHelper.makeHelper(cxt);
		new DBOpenAsync().execute(mHelper);
        mContext = cxt;
	}

    @Override
    public void addOnStorageAvailableListener(OnStorageAvailableListener listener) {
        if (mDb == null)
            mAvailableListeners.add(listener);
        else
            listener.onStorageAvailable(this);
    }

	public void close() {
		if (mDb != null)
			mDb.close();
		mDb = null;
	}
	
	@Override
	public void savePoint(Match m, Point p) throws MatchStorageNotAvailableException {
		if (mDb == null)
			throw new MatchStorageNotAvailableException();
		if (p.seq == null)
			throw new InvalidPointException();
		
		if (m.id == null)
			saveMatch(m);
		
		ContentValues vals = new ContentValues();
		vals.put("match_id", m.id);
		vals.put("seq", p.seq);
		vals.put("point", p.toString());
        vals.put("comments", p.getComments());
        mDb.replace("point", null, vals);

        requeryAdapter();
	}

	@Override
	public void saveMatch(Match m) throws MatchStorageNotAvailableException {
		if (mDb == null)
			throw new MatchStorageNotAvailableException();
		ContentValues vals = new ContentValues();
		vals.put("player1", m.player1);
		vals.put("player2", m.player2);
		vals.put("player1hand", Character.toString(m.player1hand));
		vals.put("player2hand", Character.toString(m.player2hand));
        vals.put("gender", Character.toString(m.gender));
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
        vals.put("sent", m.sent);
        vals.put("date_entered", DB_DATE_FORMAT.format(new Date()));

		if (m.id != null)
			vals.put("_id", m.id);
		
		m.id = mDb.replace("match", null, vals);
        mCursorAdapter.notifyDataSetChanged();
	}

    @Override
    public void deleteMatch(long id) throws MatchStorageNotAvailableException {
        if (mDb == null)
            throw new MatchStorageNotAvailableException();

        final String[] args = { Long.toString(id), };
        mDb.delete("match", "_id = ? ", args);
        mDb.delete("point", "match_id = ? ", args);

        requeryAdapter();
    }

    public int getIncompleteMatchCount() throws MatchStorageNotAvailableException {
		if (mDb == null)
			throw new MatchStorageNotAvailableException();
		Cursor c = mDb.rawQuery("SELECT count(*) FROM match WHERE complete = 0", null);
		
		c.moveToFirst();
		return c.getInt(0);
	}

    private Match makeMatch(Cursor c) {
        Match m = new Match(c.getInt(12), c.getInt(13) == 1, c.getInt(16) == 1);

        m.id = c.getLong(0);
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
        m.sent = c.getInt(17) != 0;
        try {
            m.gender = c.getString(18).charAt(0);
        } catch (NullPointerException e) {
            m.gender = 'W';
        } catch (IndexOutOfBoundsException e) {
            m.gender = 'W';
        }
        return m;
    }
	
	public Match retrieveMatch(long id) throws MatchStorageNotAvailableException {
		if (mDb == null)
			throw new MatchStorageNotAvailableException();
		
		final String[] args = { Long.toString(id), };
		
		Cursor c = mDb.query("match", MATCH_COLS, "_id = ?", args, null, null, null);
		if (!c.moveToFirst())
			return null;

        Match m = makeMatch(c);
        c.close();

 		final String[] point_cols = { "match_id", "seq", "point", "comments" };
 		Cursor pc = mDb.query("point", point_cols, "match_id = ?", args, null, null, null);
 		while (pc.moveToNext()) {
 			Point p = new Point(pc.getString(2));
 			p.seq = pc.getInt(1);
            p.setComments(pc.getString(3));
 			m.addPoint(p);
 		}
        pc.close();
 		 
        return m;
	}

    @Override
    public int getCount() {
        if (mDb == null)
            return 0;

        return mCursorAdapter.getCount();
    }

    @Override
    public Object getItem(int position) {
        if (mDb == null)
            return null;

        return mCursorAdapter.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        if (mDb == null)
            return 0;

        return mCursorAdapter.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mDb == null)
            return null;

        return mCursorAdapter.getView(position, convertView, parent);
    }

    private Cursor query () {
        return mDb.query("match", MATCH_COLS, null, null, null, null, "date_entered DESC");
    }

    private void requeryAdapter () {
        mCursorAdapter.swapCursor(query());
    }

    private MatchCursorAdapter matchCursorAdapterFactory () {
        return new MatchCursorAdapter(mContext, query());
    }

    private class MatchCursorAdapter extends CursorAdapter {
        public MatchCursorAdapter(Context context, Cursor c) {
            super(context, c, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(R.layout.match_listitem, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView players = (TextView) view.findViewById(R.id.players);
            TextView date = (TextView) view.findViewById(R.id.date);
            players.setText(cursor.getString(1) + " v. " + cursor.getString(2));
            if (cursor.getInt(17) == 1) // sent already
                players.setTextColor(Color.GRAY);
            else
                players.setTextColor(Color.WHITE);
            date.setText(cursor.getString(5));
        }
    }
}
