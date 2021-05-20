package com.logReplication;

import java.util.ArrayList;

public class NewLogRequest {
	
	int currentTerm;
	int responseLength;
	int previousTerm;
	ArrayList<Log> entries;
	int commitLength;
	
	public NewLogRequest(int currentTerm, int responseLength, int previousTerm, ArrayList<Log> entries, int commitLength) {
		this.currentTerm = currentTerm;
		this.previousTerm = previousTerm;
		this.entries = entries;
		this.commitLength = commitLength;
		this.responseLength = responseLength;
	}

	public int getTerm() {
		return currentTerm;
	}

	public int getresponseLength() {
		return responseLength;
	}

	public int getPreviousTerm() {
		return previousTerm;
	}

	public int getCommitlength() {
		return commitLength;
	}

	public ArrayList<Log> getLog() {
		return entries;
	}

	@Override
	public String toString() {
		return "currentTerm=" + currentTerm + ", responseLength=" + responseLength + ", previousTerm="
				+ previousTerm + ", entries=" + entries + ", commitLength=" + commitLength ;
	}
	
	

}
