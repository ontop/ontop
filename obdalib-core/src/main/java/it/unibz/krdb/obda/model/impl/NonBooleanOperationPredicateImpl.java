/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.NonBooleanOperationPredicate;

public class NonBooleanOperationPredicateImpl extends PredicateImpl implements NonBooleanOperationPredicate {

	private static final long serialVersionUID = 1L;

	public NonBooleanOperationPredicateImpl(String name) {
		super(name, 1, null);
	}

	public NonBooleanOperationPredicateImpl(String name, int arity, COL_TYPE[] types) {
		super(name, arity, types);
	}

	@Override
	public NonBooleanOperationPredicateImpl clone() {
		return this;
	}
}
