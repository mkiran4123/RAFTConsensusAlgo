package com.actions;

public class StartRaft {

	String start;

	public StartRaft(String start) {
		this.start = start;
	}

	public String getMessage() {
		return start;
	}
	
	@Override
	public String toString() {
		return "Message from client [ " + start + "]";
	}
}
