/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.BooleanOperationPredicate;


public class BooleanOperationPredicateImpl extends PredicateImpl implements BooleanOperationPredicate {

	private static final long serialVersionUID = 360476649400908702L;

	protected BooleanOperationPredicateImpl(String name) {
		super(name, 1, null);
	}

	protected BooleanOperationPredicateImpl(String name, int arity) {
		super(name, arity, null);
	}

	protected BooleanOperationPredicateImpl(String name, int arity, COL_TYPE[] types) {
		super(name, arity, types);
	}

	@Override
	public BooleanOperationPredicate clone() {
		return this;
	}
}
