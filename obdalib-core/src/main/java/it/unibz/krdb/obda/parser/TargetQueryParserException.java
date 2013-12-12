package it.unibz.krdb.obda.parser;

public class TargetQueryParserException extends Exception {
	
	private static final long serialVersionUID = 8515860690059565681L;

	public TargetQueryParserException() {
		super();
	}

	public TargetQueryParserException(String message) {
		super(message);
	}

	public TargetQueryParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public TargetQueryParserException(Throwable cause) {
		super(cause);
	}
}
