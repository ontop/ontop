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
 * This class represents the boolean AND operator.
 */
public class AndOperator extends BooleanOperator {

	private static final long serialVersionUID = -7493307826876638986L;

	@Override
	public String toString() {
		return "and";
	}
}
