package it.unibz.inf.ontop.temporal.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeSubstitutionException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.iq.transform.TemporalIQTransformer;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.temporal.iq.node.SinceNode;
import it.unibz.inf.ontop.temporal.model.TemporalRange;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class SinceNodeImpl extends TemporalOperatorWithRangeImpl implements SinceNode{

    @AssistedInject
    protected SinceNodeImpl(@Assisted TemporalRange temporalRange) {
        super(temporalRange);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {

    }

    @Override
    public QueryNode clone() {
        return null;
    }

    @Override
    public QueryNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return null;
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return null;
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return ImmutableSet.of();
    }


    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        return false;
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return false;
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return null;
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return null;
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return null;
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return false;
    }

    @Override
    public ImmutableSet<Variable> getNullableVariables(IQTree leftChild, IQTree rightChild) {
        return null;
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTransformer transformer, IQTree leftChild, IQTree rightChild) {
        if (transformer instanceof TemporalIQTransformer){
            return ((TemporalIQTransformer) transformer).transformSince(tree, this, leftChild, rightChild);
        } else {
            return transformer.transformNonStandardBinaryNonCommutativeNode(tree, this, leftChild, rightChild);
        }
    }

    @Override
    public IQTree liftBinding(IQTree leftChild, IQTree rightChild, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        return null;
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree leftChild, IQTree rightChild) {
        return null;
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, Optional<ImmutableExpression> constraint, IQTree leftChild, IQTree rightChild) {
        return null;
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree leftChild, IQTree rightChild) {
        return null;
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree leftChild, IQTree rightChild) {
        return false;
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree leftChild, IQTree rightChild) {
        return null;
    }

    @Override
    public void validateNode(IQTree leftChild, IQTree rightChild) throws InvalidIntermediateQueryException {

    }
}
