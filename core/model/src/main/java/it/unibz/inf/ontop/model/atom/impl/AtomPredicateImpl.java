package it.unibz.inf.ontop.model.atom.impl;

import it.unibz.inf.ontop.model.term.impl.PredicateImpl;
import it.unibz.inf.ontop.model.term.functionsymbol.BuiltinPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;

/**
 * TODO: in the future, make it independent from PredicateImpl
 */
public class AtomPredicateImpl extends PredicateImpl implements AtomPredicate {

    protected AtomPredicateImpl(String name, int arity) {
        super(name, arity,
                IntStream.range(0, arity).boxed()
                        .map(i -> TYPE_FACTORY.getAbstractRDFTermType())
                        .collect(ImmutableCollectors.toList()));
    }

    protected AtomPredicateImpl(Predicate datalogPredicate) {
        super(datalogPredicate.getName(),
                datalogPredicate.getArity(),
                datalogPredicate.getExpectedBaseArgumentTypes()
        );
        if (datalogPredicate instanceof BuiltinPredicate) {
            throw new IllegalArgumentException("The predicate must corresponds to a data atom!");
        }
    }
}
