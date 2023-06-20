package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;


public class TrueNodeImpl extends LeafIQTreeImpl implements TrueNode {

    private static final String PREFIX = "TRUE";
    private static final ImmutableSet<Variable> EMPTY_VARIABLE_SET = ImmutableSet.of();
    private final CoreUtilsFactory coreUtilsFactory;

    @AssistedInject
    private TrueNodeImpl(IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory, CoreUtilsFactory coreUtilsFactory) {
        super(iqTreeTools, iqFactory);
        this.coreUtilsFactory = coreUtilsFactory;
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return ImmutableSet.of();
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TrueNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        return PREFIX;
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    @Override
    public int hashCode() {
        return 12398;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return EMPTY_VARIABLE_SET;
    }

    @Override
    public IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return transformer.transformTrue(this);
    }

    @Override
    public <T> IQTree acceptTransformer(IQTreeExtendedTransformer<T> transformer, T context) {
        return transformer.transformTrue(this, context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.visitTrue(this);
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveSubstitution<Variable> freshRenamingSubstitution) {
        return this;
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, VariableGenerator variableGenerator) {
        return this;
    }

    @Override
    public ImmutableSet<Variable> getKnownVariables() {
        return ImmutableSet.of();
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
