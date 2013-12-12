package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.OBDAException;

public class ReformulationException extends OBDAException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3919418976375110086L;

	public ReformulationException(Exception cause) {
		super(cause);
	}

	public ReformulationException(String cause) {
		super(cause);
	}

	public ReformulationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
