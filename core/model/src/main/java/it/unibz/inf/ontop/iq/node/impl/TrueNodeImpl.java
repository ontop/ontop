package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;


public class TrueNodeImpl extends LeafIQTreeImpl implements TrueNode {

    private static final String PREFIX = "TRUE";

    @AssistedInject
    private TrueNodeImpl(IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory, CoreUtilsFactory coreUtilsFactory) {
        super(iqTreeTools, iqFactory, substitutionFactory, coreUtilsFactory);
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return ImmutableSet.of();
    }

    @Override
    public String toString() {
        return PREFIX;
    }

    @Override
    public int hashCode() {
        return 12398;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof TrueNodeImpl;
    }

    @Override
    public TrueNode applyFreshRenaming(InjectiveSubstitution<Variable> freshRenamingSubstitution) {
        return this;
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, VariableGenerator variableGenerator) {
        return this;
    }

    @Override
    public boolean isDistinct() {
        return true;
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

    @Override
    public VariableNullability getVariableNullability() {
        return coreUtilsFactory.createEmptyVariableNullability(ImmutableSet.of());
    }

    @Override
    public void validate() throws InvalidIntermediateQueryException {
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints() {
        return ImmutableSet.of();
    }

    @Override
    public FunctionalDependencies inferFunctionalDependencies() {
        return FunctionalDependencies.empty();
    }

    @Override
    public VariableNonRequirement getVariableNonRequirement() {
        return VariableNonRequirement.empty();
    }
}
