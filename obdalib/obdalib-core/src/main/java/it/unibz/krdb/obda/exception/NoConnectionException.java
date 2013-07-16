package it.unibz.krdb.obda.exception;

public class NoConnectionException extends Exception {

	private static final long serialVersionUID = 1L;

	public NoConnectionException() {
		super();
	}
	
	public NoConnectionException(String message) {
		super(message);
	}
}
