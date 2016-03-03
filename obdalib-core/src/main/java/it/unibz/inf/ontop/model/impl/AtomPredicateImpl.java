package it.unibz.inf.ontop.model.impl;

import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.AtomPredicate;

/**
 * TODO: in the future, make it independent from PredicateImpl
 */
public class AtomPredicateImpl extends PredicateImpl implements AtomPredicate {

    public AtomPredicateImpl(String name, int arity) {
        super(name, arity, null);
    }

    public AtomPredicateImpl(Predicate datalogPredicate) {
        super(datalogPredicate.getName(), datalogPredicate.getArity(), null);

        if (!(datalogPredicate.isDataProperty()
                || datalogPredicate.isObjectProperty()
                || datalogPredicate.isClass())
                || datalogPredicate.isAnnotationProperty())
            throw new IllegalArgumentException("The predicate must corresponds to a data atom!");
    }
}
