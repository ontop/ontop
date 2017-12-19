package it.unibz.inf.ontop.model.atom.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.impl.PredicateImpl;
import it.unibz.inf.ontop.model.term.functionsymbol.BuiltinPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.type.TermType;

/**
 * TODO: in the future, make it independent from PredicateImpl
 */
public class AtomPredicateImpl extends PredicateImpl implements AtomPredicate {

    protected AtomPredicateImpl(String name, int arity, ImmutableList<TermType> expectedBaseTypes) {
        super(name, arity, expectedBaseTypes);
    }

    protected AtomPredicateImpl(Predicate datalogPredicate) {
        super(datalogPredicate.getName(),
                datalogPredicate.getArity(),
                datalogPredicate.getExpectedBaseArgumentTypes());
        if (datalogPredicate instanceof BuiltinPredicate) {
            throw new IllegalArgumentException("The predicate must corresponds to a data atom!");
        }
    }
}
