/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
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
