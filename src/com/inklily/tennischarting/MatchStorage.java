package com.inklily.tennischarting;

public interface MatchStorage {
	public class MatchStorageNotAvailableException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	public class InvalidPointException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		
	}
	
	public void savePoint(Match m, Point p) throws MatchStorageNotAvailableException;
	public void saveMatch(Match m) throws MatchStorageNotAvailableException; 
	public Match retrieveMatch(long id)  throws MatchStorageNotAvailableException;
}
