package it.unibz.inf.ontop.spec.mapping;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

public class QuadrupleItem {
    private Predicate predicate;
    private IntermediateQuery intermediateQuery;

    public QuadrupleItem(Predicate predicate, IntermediateQuery intermediateQuery) {
        this.predicate = predicate;
        this.intermediateQuery = intermediateQuery;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public IntermediateQuery getIntermediateQuery() {
        return intermediateQuery;
    }
}
