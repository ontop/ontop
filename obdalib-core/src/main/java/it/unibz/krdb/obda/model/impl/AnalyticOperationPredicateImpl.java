package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.AnalyticOperationPredicate;


public class AnalyticOperationPredicateImpl extends PredicateImpl implements AnalyticOperationPredicate {

    protected AnalyticOperationPredicateImpl(String name, int arity, COL_TYPE[] types) {
        super(name, arity, types);
    }
}
