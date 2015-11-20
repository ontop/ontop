package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.ImmutableTerm;
import org.semanticweb.ontop.model.NonGroundFunctionalTerm;
import org.semanticweb.ontop.model.Predicate;

import static org.semanticweb.ontop.model.impl.GroundTermTools.checkNonGroundTermConstraint;

/**
 * Constraint: should contain at least one variable
 */
public class NonGroundFunctionalTermImpl extends ImmutableFunctionalTermImpl implements NonGroundFunctionalTerm {
    protected NonGroundFunctionalTermImpl(Predicate functor, ImmutableList<? extends ImmutableTerm> terms) {
        super(functor, terms);
        checkNonGroundTermConstraint(this);
    }

    protected NonGroundFunctionalTermImpl(Predicate functor, ImmutableTerm... terms) {
        super(functor, ImmutableList.copyOf(terms));
        checkNonGroundTermConstraint(this);
    }

    public NonGroundFunctionalTermImpl(Function functionalTermToClone) {
        super(functionalTermToClone);
        checkNonGroundTermConstraint(this);
    }

    @Override
    public boolean isGround() {
        return false;
    }
}
