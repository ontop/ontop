package it.unibz.krdb.obda.exception;

public class InvalidPrefixWritingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidPrefixWritingException() {
		super("Invalid URI template string.");
	}
	
	public InvalidPrefixWritingException(String message) {
		super(message);
	}
}
