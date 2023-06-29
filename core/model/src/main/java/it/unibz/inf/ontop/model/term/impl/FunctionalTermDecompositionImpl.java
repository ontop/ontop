package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.Substitution;

import javax.annotation.Nonnull;

public class FunctionalTermDecompositionImpl implements ImmutableFunctionalTerm.FunctionalTermDecomposition {

    private final ImmutableTerm liftableTerm;
    private final Substitution<ImmutableFunctionalTerm> substitution;

    protected FunctionalTermDecompositionImpl(ImmutableTerm injectiveTerm,
                                              @Nonnull Substitution<ImmutableFunctionalTerm> substitution) {
        this.liftableTerm = injectiveTerm;
        this.substitution = substitution;
    }

    @Override
    public ImmutableTerm getLiftableTerm() {
        return liftableTerm;
    }

    @Override
    public Substitution<ImmutableFunctionalTerm> getSubstitution() {
        return substitution;
    }
}
