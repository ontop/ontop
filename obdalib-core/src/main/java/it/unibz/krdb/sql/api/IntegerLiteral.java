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
 * This class represents the literal of integer value.
 */
public class IntegerLiteral extends NumericLiteral {

	private static final long serialVersionUID = 4363575050294176802L;
	
	/**
	 * Integer value
	 */
	protected Integer value;

	/**
	 * Constructor
	 * 
	 * @param value
	 *            Integer value
	 */
	public IntegerLiteral(String value) {
		set(new Integer(value));
	}

	/**
	 * Set the Integer value
	 * 
	 * @param value
	 *            Integer value
	 */
	public void set(Integer value) {
		this.value = value;
	}

	/**
	 * Get the Integer value
	 * 
	 * @return Integer value
	 */
	public Integer get() {
		return value;
	}

	@Override
	public String toString() {
		return get().toString();
	}
}
