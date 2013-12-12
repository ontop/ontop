package it.unibz.krdb.obda.exception;

public class QueryResultException extends Exception {

	private static final long serialVersionUID = 1L;

	public QueryResultException() {
	}

	public QueryResultException(String message) {
		super(message);
	}

	public QueryResultException(Throwable cause) {
		super(cause);
	}

	public QueryResultException(String message, Throwable cause) {
		super(message, cause);
	}
}
