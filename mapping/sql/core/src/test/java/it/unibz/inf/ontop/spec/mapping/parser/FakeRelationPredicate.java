package it.unibz.inf.ontop.spec.mapping.parser;

import it.unibz.inf.ontop.model.term.impl.PredicateImpl;

import javax.annotation.Nonnull;

/**
 * For low-level tests only!
 */
public class FakeRelationPredicate extends PredicateImpl {

    protected FakeRelationPredicate(@Nonnull String name, int arity) {
        super(name, arity);
    }
}
