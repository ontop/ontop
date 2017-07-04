package it.unibz.inf.ontop.model.impl;

import it.unibz.inf.ontop.model.predicate.BuiltinPredicate;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * TODO: in the future, make it independent from PredicateImpl
 */
public class AtomPredicateImpl extends PredicateImpl implements AtomPredicate {

    protected AtomPredicateImpl(String name, int arity) {
        super(name, arity, null);
    }

    protected AtomPredicateImpl(String name, int arity, COL_TYPE[] types) {
        super(name, arity, types);
    }

    protected AtomPredicateImpl(Predicate datalogPredicate) {
        super(datalogPredicate.getName(),
                datalogPredicate.getArity(),
                datalogPredicate.getTypes()
        );

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
