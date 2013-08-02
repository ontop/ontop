/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlapi3;

import org.semanticweb.owlapi.model.OWLException;

/**
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 *
 */
public class OntopOWLException extends OWLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3822597596242502263L;

	/**
	 * 
	 */
	public OntopOWLException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public OntopOWLException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public OntopOWLException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public OntopOWLException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
