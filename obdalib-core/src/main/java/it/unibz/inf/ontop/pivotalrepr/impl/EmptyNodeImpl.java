package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import static it.unibz.inf.ontop.pivotalrepr.NodeTransformationProposedState.DELETE;
import static it.unibz.inf.ontop.pivotalrepr.SubstitutionResults.LocalAction.NO_CHANGE;

public class EmptyNodeImpl extends QueryNodeImpl implements EmptyNode {

    private static final String PREFIX = "EMPTY ";
    private final ImmutableSet<Variable> projectedVariables;

    public EmptyNodeImpl(ImmutableSet<Variable> projectedVariables) {
        this.projectedVariables = projectedVariables;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public EmptyNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException, NotNeededNodeException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        return projectedVariables;
    }

    @Override
    public SubstitutionResults<EmptyNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) {
        return new SubstitutionResultsImpl<>(NO_CHANGE);
    }

    @Override
    public SubstitutionResults<EmptyNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
        ImmutableSet<Variable> newProjectedVariables = projectedVariables.stream()
                .map(v -> substitution.apply(v))
                .filter(v -> v instanceof Variable)
                .map(v -> (Variable) v)
                .collect(ImmutableCollectors.toSet());

        EmptyNode newNode = new EmptyNodeImpl(newProjectedVariables);
        return new SubstitutionResultsImpl<>(newNode);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        if (node instanceof EmptyNode) {
            return projectedVariables.equals(((EmptyNode) node).getProjectedVariables());
        }
        return false;
    }

    @Override
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
        throw new UnsupportedOperationException("A EmptyNode is not expected to have a child");
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
    public ImmutableSet<Variable> getProjectedVariables() {
        return projectedVariables;
    }
}
