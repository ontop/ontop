package it.unibz.krdb.obda.owlrefplatform.exception;

public class MemoryLowException extends Exception {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 2004858092400545767L;

	public MemoryLowException(String msg) {
		super(msg);
	}

	public MemoryLowException() {
		super();
	}
}
