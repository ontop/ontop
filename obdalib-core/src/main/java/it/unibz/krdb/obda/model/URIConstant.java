/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model;


/**
 * Provides an interface for storing the URI constant.
 */
public interface URIConstant extends ObjectConstant {

	/**
	 * Get the URI object from this constant.
	 *
	 * @return the URI object.
	 */
	public String getURI();
}
