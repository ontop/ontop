package it.unibz.krdb.obda.exception;

public class NoDatasourceSelectedException extends Exception {

	private static final long serialVersionUID = 1L;

	public NoDatasourceSelectedException(String msg) {
		super(msg);
	}
}
