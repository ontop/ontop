/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model;

public class OBDAException extends Exception {

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
