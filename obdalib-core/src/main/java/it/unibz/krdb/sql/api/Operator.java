/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql.api;

/**
 * A base class for operators in the relational algebra
 * expression.
 */
public abstract class Operator extends RelationalAlgebra {
	
	private static final long serialVersionUID = -707166944279903524L;
	
	protected String alias;

	public Operator() {
		// NO-OP
	}

	@Override
	public Operator clone() {
		return null;
	}
}
