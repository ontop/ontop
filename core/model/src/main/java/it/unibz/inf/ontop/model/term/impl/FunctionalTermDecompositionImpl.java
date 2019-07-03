package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class FunctionalTermDecompositionImpl implements ImmutableFunctionalTerm.FunctionalTermDecomposition {

    private final ImmutableTerm liftableTerm;
    @Nullable
    private final ImmutableMap<Variable, ImmutableFunctionalTerm> subTermSubstitutionMap;

    protected FunctionalTermDecompositionImpl(ImmutableTerm injectiveTerm,
                                              @Nonnull ImmutableMap<Variable, ImmutableFunctionalTerm> subTermSubstitutionMap) {
        this.liftableTerm = injectiveTerm;
        this.subTermSubstitutionMap = subTermSubstitutionMap;
    }

    protected FunctionalTermDecompositionImpl(ImmutableTerm liftableTerm) {
        this.liftableTerm = liftableTerm;
        this.subTermSubstitutionMap = null;
    }

    @Override
    public ImmutableTerm getLiftableTerm() {
        return liftableTerm;
    }

    @Override
    public Optional<ImmutableMap<Variable, ImmutableFunctionalTerm>> getSubTermSubstitutionMap() {
        return Optional.ofNullable(subTermSubstitutionMap);
    }
}
