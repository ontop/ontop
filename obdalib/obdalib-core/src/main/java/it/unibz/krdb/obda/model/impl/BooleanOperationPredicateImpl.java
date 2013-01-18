package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.BooleanOperationPredicate;

import java.net.URI;

import com.hp.hpl.jena.iri.IRI;

public class BooleanOperationPredicateImpl extends PredicateImpl implements BooleanOperationPredicate {

	private static final long serialVersionUID = 1L;

	protected BooleanOperationPredicateImpl(IRI name) {
		super(name, 1, null);
	}

	protected BooleanOperationPredicateImpl(IRI name, int arity) {
		super(name, arity, null);
	}

	protected BooleanOperationPredicateImpl(IRI name, int arity, COL_TYPE[] types) {
		super(name, arity, types);
	}

	@Override
	public BooleanOperationPredicate clone() {
		return this;
	}
}