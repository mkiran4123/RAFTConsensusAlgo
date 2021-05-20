package com.election;

public class ResponseVote {
	
	int currentTerm;
	boolean voted;

	public ResponseVote(int currentTerm, boolean voted) {
		this.currentTerm = currentTerm;
		this.voted = voted;
	}

	public boolean isVoated() {
		return voted;
	}

	public int getTerm() {
		return currentTerm;
	}

}
