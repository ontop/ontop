package it.unibz.inf.ontop.model.impl;

import it.unibz.inf.ontop.model.BuiltinPredicate;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.AtomPredicate;

/**
 * TODO: in the future, make it independent from PredicateImpl
 */
public class AtomPredicateImpl extends PredicateImpl implements AtomPredicate {

    protected AtomPredicateImpl(String name, int arity) {
        super(name, arity, null);
    }

    protected AtomPredicateImpl(Predicate datalogPredicate) {
        super(datalogPredicate.getName(), datalogPredicate.getArity(), null);

//        if (!(datalogPredicate.isDataProperty()
//                || datalogPredicate.isObjectProperty()
//                || datalogPredicate.isClass())
//                || datalogPredicate.isAnnotationProperty())
//            throw new IllegalArgumentException("The predicate must corresponds to a data atom!");
        if (datalogPredicate instanceof BuiltinPredicate) {
            throw new IllegalArgumentException("The predicate must corresponds to a data atom!");
        }
    }
}
