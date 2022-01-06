package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Objects;


public class EmptyNodeImpl extends LeafIQTreeImpl implements EmptyNode {

    private static final String PREFIX = "EMPTY ";
    private final ImmutableSet<Variable> projectedVariables;
    private final ConstructionNodeTools constructionNodeTools;
    private final CoreUtilsFactory coreUtilsFactory;

    @AssistedInject
    private EmptyNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables,
                          IQTreeTools iqTreeTools, ConstructionNodeTools constructionNodeTools,
                          IntermediateQueryFactory iqFactory, CoreUtilsFactory coreUtilsFactory) {
        super(iqTreeTools, iqFactory);
        this.projectedVariables = projectedVariables;
        this.constructionNodeTools = constructionNodeTools;
        this.coreUtilsFactory = coreUtilsFactory;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public EmptyNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return projectedVariables;
    }

    @Override
    public String toString() {
        return PREFIX + projectedVariables;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return transformer.transformEmpty(this);
    }

    @Override
    public <T> IQTree acceptTransformer(IQTreeExtendedTransformer<T> transformer, T context) {
        return transformer.transformEmpty(this, context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.visitEmpty(this);
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        ImmutableSet<Variable> newVariables = projectedVariables.stream()
                .map(freshRenamingSubstitution::applyToVariable)
                .collect(ImmutableCollectors.toSet());

        return newVariables.equals(projectedVariables)
                ? this
                : iqFactory.createEmptyNode(newVariables);
    }

    @Override
    public IQTree applyFreshRenamingToAllVariables(InjectiveVar2VarSubstitution freshRenamingSubstitution) {
        return applyFreshRenaming(freshRenamingSubstitution);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        return iqFactory.createEmptyNode(
                constructionNodeTools.computeNewProjectedVariables(descendingSubstitution, projectedVariables));
    }

    @Override
    public ImmutableSet<Variable> getKnownVariables() {
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
    public ImmutableSet<Variable> getNotInternallyRequiredVariables() {
        return getVariables();
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmptyNodeImpl emptyNode = (EmptyNodeImpl) o;
        return projectedVariables.equals(emptyNode.projectedVariables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectedVariables);
    }
}
