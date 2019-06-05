package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class AggregationNodeImpl extends CompositeQueryNodeImpl implements AggregationNode {


    private final ImmutableSet<Variable> projectedVariables;
    private final ImmutableSet<Variable> groupingVariables;
    private final ImmutableSubstitution<ImmutableFunctionalTerm> substitution;
    private final ImmutableSet<Variable> childVariables;

    @AssistedInject
    protected AggregationNodeImpl(@Assisted ImmutableSet<Variable> groupingVariables,
                                  @Assisted ImmutableSubstitution<ImmutableFunctionalTerm> substitution,
                                  SubstitutionFactory substitutionFactory, IntermediateQueryFactory iqFactory) {
        super(substitutionFactory, iqFactory);
        this.groupingVariables = groupingVariables;
        this.substitution = substitution;
        this.projectedVariables = Sets.union(groupingVariables, substitution.getDomain()).immutableCopy();
        this.childVariables = Sets.union(groupingVariables,
                substitution.getImmutableMap().values().stream()
                .flatMap(ImmutableTerm::getVariableStream)
                        .collect(ImmutableCollectors.toSet())).immutableCopy();
    }

    @Override
    public IQTree normalizeForOptimization(IQTree child, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                               IQTree child) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public VariableNullability getVariableNullability(IQTree child) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return substitution.isDefining(variable)
                || (groupingVariables.contains(variable) && child.isConstructed(variable));
    }

    @Override
    public boolean isDistinct(IQTree child) {
        return true;
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public AggregationNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return Sets.union(getChildVariables(), substitution.getDomain()).immutableCopy();
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return substitution.getDomain();
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        throw new RuntimeException("TODO: implement");
    }

    /**
     * TODO: implement seriously
     */
    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
        // TODO: check that the grouping variables and the substitution domain are disjoint
    }

    @Override
    public ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child) {
        throw new RuntimeException("TODO: implement");
    }

    @Override
    public IQTree removeDistincts(IQTree child, IQProperties iqProperties) {
        IQTree newChild = child.removeDistincts();

        IQProperties newProperties = newChild.isEquivalentTo(child)
                ? iqProperties.declareDistinctRemovalWithoutEffect()
                : iqProperties.declareDistinctRemovalWithEffect();

        return iqFactory.createUnaryIQTree(this, newChild, newProperties);
    }

    @Override
    public ImmutableSubstitution<ImmutableFunctionalTerm> getSubstitution() {
        return substitution;
    }

    @Override
    public ImmutableSet<Variable> getGroupingVariables() {
        return groupingVariables;
    }

    @Override
    public ImmutableSet<Variable> getChildVariables() {
        return childVariables;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public AggregationNode clone() {
        throw new RuntimeException("TODO: implement");
    }
}
