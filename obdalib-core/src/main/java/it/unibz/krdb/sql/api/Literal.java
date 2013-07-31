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
 * A shared abstract class to represent literal values.
 */
public abstract class Literal implements IValueExpression {
	
	private static final long serialVersionUID = 3027236534360355748L;

	public abstract Object get();
}
