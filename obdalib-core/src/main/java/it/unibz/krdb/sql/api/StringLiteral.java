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
 * This class represents the literal of string value.
 */
public class StringLiteral extends Literal {
	
	private static final long serialVersionUID = -1560568410005060451L;
	
	/**
	 * The string value.
	 */
	protected String value;

	public StringLiteral(String value) {
		set(value);
	}

	public void set(String value) {
		this.value = value;
	}

	public Object get() {
		return value;
	}

	@Override
	public String toString() {
		return String.format("'%s'", value);
	}
}
