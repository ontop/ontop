/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.basicoperations;

import it.unibz.krdb.sql.Reference;

public class BrokenForeignKeyException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public BrokenForeignKeyException() {
		super();
	}
	
	public BrokenForeignKeyException(Reference reference, String message) {
		super("Broken integrity constraint: " + reference.getReferenceName());
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder(super.getMessage());
		sb.append(" ");
		sb.append("(Reason: ");
		sb.append(message);
		sb.append(")");
		return sb.toString();
	}
}
