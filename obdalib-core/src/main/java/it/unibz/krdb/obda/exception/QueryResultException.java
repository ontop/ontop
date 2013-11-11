/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.exception;

public class QueryResultException extends Exception {

	private static final long serialVersionUID = 1L;

	public QueryResultException() {
	}

	public QueryResultException(String message) {
		super(message);
	}

	public QueryResultException(Throwable cause) {
		super(cause);
	}

	public QueryResultException(String message, Throwable cause) {
		super(message, cause);
	}
}
