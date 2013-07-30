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
 * This class represents the boolean OR operator.
 */
public class OrOperator extends BooleanOperator {

	private static final long serialVersionUID = -8247317849319172159L;

	@Override
	public String toString() {
		return "or";
	}
}
