package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.NumericalOperationPredicate;

import com.hp.hpl.jena.iri.IRI;

public class NumericalOperationPredicateImpl extends PredicateImpl implements NumericalOperationPredicate {

	private static final long serialVersionUID = 1L;

	protected NumericalOperationPredicateImpl(IRI name, int arity) {
		super(name, arity, null);
	}
	
	protected NumericalOperationPredicateImpl(IRI name, int arity, COL_TYPE[] types) {
		super(name, arity, types);
	}

	@Override
	public NumericalOperationPredicate clone() {
		return this;
	}
}
