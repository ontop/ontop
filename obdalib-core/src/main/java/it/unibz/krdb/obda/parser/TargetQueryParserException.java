/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.parser;

public class TargetQueryParserException extends Exception {
	
	private static final long serialVersionUID = 8515860690059565681L;

	public TargetQueryParserException() {
		super();
	}

	public TargetQueryParserException(String message) {
		super(message);
	}

	public TargetQueryParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public TargetQueryParserException(Throwable cause) {
		super(cause);
	}
}
