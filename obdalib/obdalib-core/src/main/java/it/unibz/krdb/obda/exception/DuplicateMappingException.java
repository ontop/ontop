package it.unibz.krdb.obda.exception;

public class DuplicateMappingException extends Exception {

	private static final long serialVersionUID = 1L;

	public DuplicateMappingException() {
		super();
	}
	
	public DuplicateMappingException(String message) {
		super(message);
	}
}
