package it.unibz.inf.ontop.substitution;

import it.unibz.inf.ontop.model.term.impl.PredicateImpl;

import javax.annotation.Nonnull;

public class OntopModelTestPredicate extends PredicateImpl {

    protected OntopModelTestPredicate(@Nonnull String name, int arity) {
        super(name, arity);
    }
}
