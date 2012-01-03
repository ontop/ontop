package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.BooleanOperationPredicate;

import java.net.URI;

public class BooleanOperationPredicateImpl extends PredicateImpl implements BooleanOperationPredicate {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2415971199358517200L;

	protected BooleanOperationPredicateImpl(URI name, int arity) {
		super(name, arity, null);
	}

	protected BooleanOperationPredicateImpl(URI name, int arity, COL_TYPE[] types) {
		super(name, arity, types);
	}

	@Override
	public BooleanOperationPredicate clone() {
		return this;
	}
}
