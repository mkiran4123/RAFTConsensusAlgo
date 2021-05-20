package com.election;

public class RequestForVotes {
	
	int currentTerm;
	int size;
	int lastTerm;

	public RequestForVotes(int currentTerm, int size, int lastTerm) {
		this.currentTerm = currentTerm;
		this.size = size;
		this.lastTerm = lastTerm;
	}

	public int getLastTerm() {
		
		return lastTerm;
	}

	public int getCurrentTerm() {
		return currentTerm;
	}

	public int getLogLength() {
		return size;
	}

}
