package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import javax.annotation.Nonnull;

public class FunctionalTermDecompositionImpl implements ImmutableFunctionalTerm.FunctionalTermDecomposition {

    private final ImmutableTerm liftableTerm;
    private final ImmutableSubstitution<ImmutableFunctionalTerm> substitution;

    protected FunctionalTermDecompositionImpl(ImmutableTerm injectiveTerm,
                                              @Nonnull ImmutableSubstitution<ImmutableFunctionalTerm> substitution) {
        this.liftableTerm = injectiveTerm;
        this.substitution = substitution;
    }

    @Override
    public ImmutableTerm getLiftableTerm() {
        return liftableTerm;
    }

    @Override
    public ImmutableSubstitution<ImmutableFunctionalTerm> getSubstitution() {
        return substitution;
    }
}
