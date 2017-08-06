package it.unibz.inf.ontop.temporal.model.impl;

import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.temporal.model.TemporalQuery;

public class TemporalQueryImpl implements TemporalQuery{

    private final Predicate queryPredicate;

    TemporalQueryImpl(Predicate queryPredicate){
        this.queryPredicate = queryPredicate;
    }

    @Override
    public Predicate getPredicate() {
        return this.queryPredicate;
    }
}
