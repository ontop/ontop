package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.FunctionalTermSimplification;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

public class FunctionalTermSimplificationImpl implements FunctionalTermSimplification {
    private final ImmutableTerm simplifiedTerm;
    private final ImmutableSet<Variable> simplifiableVariables;

    public FunctionalTermSimplificationImpl(ImmutableTerm simplifiedTerm,
                                            ImmutableSet<Variable> simplifiableVariables) {
        this.simplifiedTerm = simplifiedTerm;
        this.simplifiableVariables = simplifiableVariables;
    }

    @Override
    public ImmutableTerm getSimplifiedTerm() {
        return simplifiedTerm;
    }

    @Override
    public ImmutableSet<Variable> getSimplifiableVariables() {
        return simplifiableVariables;
    }
}
