package unibz.inf.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import unibz.inf.ontop.model.Function;
import unibz.inf.ontop.model.Predicate;
import unibz.inf.ontop.model.ImmutableTerm;
import unibz.inf.ontop.model.NonGroundFunctionalTerm;

import static unibz.inf.ontop.model.impl.GroundTermTools.checkNonGroundTermConstraint;

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
