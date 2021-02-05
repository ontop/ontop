package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonGroundFunctionalTerm;

import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.checkNonGroundTermConstraint;

/**
 * Constraint: should contain at least one variable
 */
public class NonGroundFunctionalTermImpl extends ImmutableFunctionalTermImpl implements NonGroundFunctionalTerm {
    protected NonGroundFunctionalTermImpl(FunctionSymbol functor, ImmutableList<? extends ImmutableTerm> terms,
                                          TermFactory termFactory) {
        super(functor, terms, termFactory);
        checkNonGroundTermConstraint(this);
    }

    protected NonGroundFunctionalTermImpl(TermFactory termFactory, FunctionSymbol functor, ImmutableTerm... terms) {
        super(functor, ImmutableList.copyOf(terms), termFactory);
        checkNonGroundTermConstraint(this);
    }

    @Override
    public boolean isGround() {
        return false;
    }
}
