package it.unibz.krdb.obda.owlrefplatform.owlapi2;

import org.semanticweb.owl.inference.OWLReasonerException;

public class OBDAOWLReformulationPlatformException extends OWLReasonerException {


	/**
	 * Is used to forward exceptions coming from the the reformulation
	 * platform reasoner to the owl api
	 */

	private static final long serialVersionUID = -8272824527128285690L;

	public OBDAOWLReformulationPlatformException(String message) {
		super(message);
	}

	public OBDAOWLReformulationPlatformException(Throwable cause) {
		super(cause);
	}

	public OBDAOWLReformulationPlatformException(String message, Throwable cause) {
		super(message, cause);
	}

}
