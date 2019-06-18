package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class FunctionalTermDecompositionImpl implements ImmutableFunctionalTerm.FunctionalTermDecomposition {

    private final ImmutableFunctionalTerm injectiveFunctionalTerm;
    @Nullable
    private final ImmutableMap<Variable, ImmutableFunctionalTerm> subTermSubstitutionMap;

    protected FunctionalTermDecompositionImpl(ImmutableFunctionalTerm injectiveFunctionalTerm,
                                              @Nonnull ImmutableMap<Variable, ImmutableFunctionalTerm> subTermSubstitutionMap) {
        this.injectiveFunctionalTerm = injectiveFunctionalTerm;
        this.subTermSubstitutionMap = subTermSubstitutionMap;
    }

    protected FunctionalTermDecompositionImpl(ImmutableFunctionalTerm injectiveFunctionalTerm) {
        this.injectiveFunctionalTerm = injectiveFunctionalTerm;
        this.subTermSubstitutionMap = null;
    }

    @Override
    public ImmutableFunctionalTerm getLiftableTerm() {
        return injectiveFunctionalTerm;
    }

    @Override
    public Optional<ImmutableMap<Variable, ImmutableFunctionalTerm>> getSubTermSubstitutionMap() {
        return Optional.ofNullable(subTermSubstitutionMap);
    }
}
