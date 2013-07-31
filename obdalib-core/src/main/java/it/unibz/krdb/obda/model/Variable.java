/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model;

/**
 * This class defines a type of {@link NewLiteral} in which it expresses a quantity
 * that during a calculation is assumed to vary or be capable of varying in
 * value.
 */
public interface Variable extends NewLiteral {

	public String getName();
}
