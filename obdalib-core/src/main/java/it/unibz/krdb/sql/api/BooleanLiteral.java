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
 * This class represents the literal of boolean value.
 */
public class BooleanLiteral extends Literal {
	
	private static final long serialVersionUID = 8593416410506376356L;
	
	/**
	 * The boolean value.
	 */
	protected boolean value;

	public BooleanLiteral(boolean value) {
		this.set(value);
	}

	public void set(boolean value) {
		this.value = value;
	}

	public Object get() {
		return value;
	}

	@Override
	public String toString() {
		return value ? "true" : "false";
	}
}
