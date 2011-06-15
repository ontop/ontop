package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.BooleanOperationPredicate;

import java.net.URI;

public class BooleanOperationPredicateImpl extends PredicateImpl implements BooleanOperationPredicate {

	protected BooleanOperationPredicateImpl(URI name, int arity) {
		super(name, arity);
	}
	
	@Override
	public BooleanOperationPredicate clone() {
		return new BooleanOperationPredicateImpl(URI.create(getName().toString()), getArity()); 
	}
}
