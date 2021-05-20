package com.logReplication;

public class LogResponse {
	
	int term;
	int acknowledgement;
	boolean isSuccess;

	public LogResponse(int term, int acknowledgement, boolean isSuccess) {
		this.term = term;
		this.acknowledgement = acknowledgement;
		this.isSuccess = isSuccess;
	}

	public int getTerm() {
		return term;
	}

	public void setTerm(int term) {
		this.term = term;
	}

	public int getAcknowledgement() {
		return acknowledgement;
	}

	public void setAcknowledgement(int acknowledgement) {
		this.acknowledgement = acknowledgement;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}



}
