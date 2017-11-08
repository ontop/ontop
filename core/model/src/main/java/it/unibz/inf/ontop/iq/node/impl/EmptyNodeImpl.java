package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.DefaultSubstitutionResults;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public class EmptyNodeImpl extends QueryNodeImpl implements EmptyNode {

    private static final String PREFIX = "EMPTY ";
    private final ImmutableSet<Variable> projectedVariables;

    @AssistedInject
    private EmptyNodeImpl(@Assisted ImmutableSet<Variable> projectedVariables) {
        this.projectedVariables = projectedVariables;
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
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return projectedVariables;
    }

    @Override
    public SubstitutionResults<EmptyNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) {
        return DefaultSubstitutionResults.noChange();
    }

    @Override
    public SubstitutionResults<EmptyNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
        ImmutableSet<Variable> newProjectedVariables = projectedVariables.stream()
                .map(substitution::apply)
                .filter(v -> v instanceof Variable)
                .map(v -> (Variable) v)
                .collect(ImmutableCollectors.toSet());

        EmptyNode newNode = new EmptyNodeImpl(newProjectedVariables);
        return DefaultSubstitutionResults.newNode(newNode);
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        if (getVariables().contains(variable))
            return true;
        else
            throw new IllegalArgumentException("The variable " + variable + " is not projected by " + this);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        if (node instanceof EmptyNode) {
            return projectedVariables.equals(((EmptyNode) node).getVariables());
        }
        return false;
    }

    @Override
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
        throw new UnsupportedOperationException("An EmptyNode is not expected to have a child");
    }

    @Override
    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueChild) {
        throw new UnsupportedOperationException("An EmptyNode is not expected to have a child");
    }

    @Override
    public EmptyNode clone() {
        return new EmptyNodeImpl(projectedVariables);
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
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return getLocallyRequiredVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }
}
