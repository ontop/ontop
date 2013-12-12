package it.unibz.krdb.obda.owlrefplatform.exception;

public class OBDAOWLReformulaionPlatformFactoryException extends
		RuntimeException {

	/**
	 * Is used to forward exceptions during the creation of the reformulation
	 * platform factory to the owl api
	 */
	private static final long serialVersionUID = 1162920026840673947L;
	
	public OBDAOWLReformulaionPlatformFactoryException(Throwable cause){
		super(cause);
	}

}
