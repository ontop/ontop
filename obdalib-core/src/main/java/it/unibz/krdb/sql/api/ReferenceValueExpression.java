/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql.api;

public class ReferenceValueExpression extends AbstractValueExpression {

	private static final long serialVersionUID = -3324178278393020165L;

	@Override
	public void putSpecification(Object obj) {
		// NO-OP
	}

	@Override
	public String toString() {
		ColumnReference column = factors.get(0); // always has one element.
		return column.toString();
	}	
}
