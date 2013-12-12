package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.BNodePredicate;

public class BNodePredicateImpl extends PredicateImpl implements BNodePredicate {

	private static final long serialVersionUID = -1546325236776439443L;

	public BNodePredicateImpl(int arity) {
		// TODO: BAD CODE! Predicate shouldn't store the arity and the type.
		super(OBDAVocabulary.QUEST_BNODE, arity, null);
	}

	@Override
	public BNodePredicateImpl clone() {
		return this;
	}
}
