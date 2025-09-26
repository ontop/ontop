package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
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

import java.util.Objects;


public class EmptyNodeImpl extends LeafIQTreeImpl implements EmptyNode {

    private static final String PREFIX = "EMPTY ";
    private final ImmutableSet<Variable> projectedVariables;

    @AssistedInject
    private EmptyNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                          IQTreeTools iqTreeTools,
                          IntermediateQueryFactory iqFactory, CoreUtilsFactory coreUtilsFactory, SubstitutionFactory substitutionFactory) {
        super(iqTreeTools, iqFactory, substitutionFactory, coreUtilsFactory);
        this.projectedVariables = projectedVariables;
    }


    @Override
    public String toString() {
        return PREFIX + projectedVariables;
    }

    @Override
    public EmptyNode applyFreshRenaming(InjectiveSubstitution<Variable> freshRenamingSubstitution) {
        ImmutableSet<Variable> newVariables = substitutionFactory.apply(freshRenamingSubstitution, projectedVariables);
        return newVariables.equals(projectedVariables)
                ? this
                : iqFactory.createEmptyNode(newVariables);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, VariableGenerator variableGenerator) {
        return iqFactory.createEmptyNode(
                DownPropagation.computeProjectedVariables(descendingSubstitution, projectedVariables));
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public boolean isDistinct() {
        return true;
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return true;
    }

    @Override
    public VariableNullability getVariableNullability() {
        return coreUtilsFactory.createVariableNullability(ImmutableSet.of(projectedVariables), projectedVariables);
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
        return VariableNonRequirement.of(projectedVariables);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof EmptyNodeImpl) {
            EmptyNodeImpl emptyNode = (EmptyNodeImpl) o;
            return projectedVariables.equals(emptyNode.projectedVariables);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectedVariables);
    }
}
