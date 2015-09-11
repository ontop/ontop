package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.NonGroundFunctionalTerm;
import org.semanticweb.ontop.model.Predicate;

/**
 * Constraint: should contain at least one variable
 */
public class NonGroundFunctionalTermImpl extends ImmutableFunctionalTermImpl implements NonGroundFunctionalTerm {
    protected NonGroundFunctionalTermImpl(Predicate functor, ImmutableList<? extends ImmutableTerm> terms) {
        super(functor, terms);
        checkNonGroundTermConstraint(this);
    }

    protected NonGroundFunctionalTermImpl(Predicate functor, ImmutableTerm... terms) {
        super(functor, terms);
        checkNonGroundTermConstraint(this);
    }

    public NonGroundFunctionalTermImpl(Function functionalTermToClone) {
        super(functionalTermToClone);
        checkNonGroundTermConstraint(this);
    }

    private static void checkNonGroundTermConstraint(NonGroundFunctionalTerm term) throws IllegalArgumentException {
        if (term.getVariables().isEmpty()) {
            throw new IllegalArgumentException("A NonGroundFunctionalTerm must contain at least one variable");
        }
    }
}
