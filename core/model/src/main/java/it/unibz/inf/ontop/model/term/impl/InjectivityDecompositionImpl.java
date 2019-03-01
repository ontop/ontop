package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class InjectivityDecompositionImpl implements ImmutableFunctionalTerm.InjectivityDecomposition {

    private final ImmutableFunctionalTerm injectiveFunctionalTerm;
    @Nullable
    private final ImmutableMap<Variable, ImmutableTerm> subTermSubstitutionMap;

    protected InjectivityDecompositionImpl(ImmutableFunctionalTerm injectiveFunctionalTerm,
                                           @Nonnull ImmutableMap<Variable, ImmutableTerm> subTermSubstitutionMap) {
        this.injectiveFunctionalTerm = injectiveFunctionalTerm;
        this.subTermSubstitutionMap = subTermSubstitutionMap;
    }

    protected InjectivityDecompositionImpl(ImmutableFunctionalTerm injectiveFunctionalTerm) {
        this.injectiveFunctionalTerm = injectiveFunctionalTerm;
        this.subTermSubstitutionMap = null;
    }

    @Override
    public ImmutableFunctionalTerm getInjectiveTerm() {
        return injectiveFunctionalTerm;
    }

    @Override
    public Optional<ImmutableMap<Variable, ImmutableTerm>> getSubTermSubstitutionMap() {
        return Optional.ofNullable(subTermSubstitutionMap);
    }
}
