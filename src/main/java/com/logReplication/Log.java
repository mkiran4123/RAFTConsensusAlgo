package com.logReplication;

public class Log {
	
	int currentTerm;
	String message;

	public Log(int currentTerm, String message) {
		this.currentTerm = currentTerm;
		this.message = message;
	}

	public int getTerm() {
		return currentTerm;
	}

	@Override
	public String toString() {
		return "[ currentTerm=" + currentTerm + ", message=" + message + " ]";
	}

}
