package com.inklily.tennischarting;

import android.widget.ListAdapter;

public interface MatchStorage extends ListAdapter {
	public class MatchStorageNotAvailableException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	public class InvalidPointException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		
	}

    public interface OnStorageAvailableListener {
        public void onStorageAvailable(MatchStorage storage);
    }

    public void addOnStorageAvailableListener(OnStorageAvailableListener listener);
	public void savePoint(Match m, Point p) throws MatchStorageNotAvailableException;
	public void saveMatch(Match m) throws MatchStorageNotAvailableException;
    public void deleteMatch(long id) throws MatchStorageNotAvailableException;
	public Match retrieveMatch(long id)  throws MatchStorageNotAvailableException;
}
