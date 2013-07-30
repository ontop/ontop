/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql.api;

public class CollectionValueExpression extends AbstractValueExpression {

	private static final long serialVersionUID = -8519994184134506857L;
	
	/**
	 * The name of the function operation.
	 */
	private String functionOp = "";
	
	@Override
	public void putSpecification(Object obj) {
		functionOp = (String)obj;
	}
	
	public String getSpecification() {
		return functionOp;
	}

	@Override
	public String toString() {
		return functionOp + "(" + factors.get(0) + ")";
	}
}
