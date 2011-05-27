package it.unibz.krdb.obda.owlrefplatform.core.abox;

public class AboxDumpException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1167050541326123083L;

	public AboxDumpException(String msg, Throwable ex){
		super(msg, ex);
	}
	
	public AboxDumpException(String msg){
		super(msg);
	}
}
