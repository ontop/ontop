package it.unibz.krdb.obda.model;

public class OBDAException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -378610789773679125L;

	public OBDAException(Exception cause) {
		super(cause);
	}

	public OBDAException(String cause) {
		super(cause);
	}

	public OBDAException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
