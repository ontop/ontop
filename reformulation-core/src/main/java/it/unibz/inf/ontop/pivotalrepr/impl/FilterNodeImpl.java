package it.unibz.inf.ontop.pivotalrepr.impl;


import java.util.Optional;

import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator.Evaluation;
import it.unibz.inf.ontop.pivotalrepr.*;

import static it.unibz.inf.ontop.pivotalrepr.NodeTransformationProposedState.DELETE;
import static it.unibz.inf.ontop.pivotalrepr.SubstitutionResults.LocalAction.DECLARE_AS_EMPTY;

public class FilterNodeImpl extends JoinOrFilterNodeImpl implements FilterNode {

    private static final String FILTER_NODE_STR = "FILTER";

    public FilterNodeImpl(ImmutableExpression filterCondition) {
        super(Optional.of(filterCondition));
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public FilterNode clone() {
        return new FilterNodeImpl(getOptionalFilterCondition().get());
    }

    @Override
    public FilterNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableExpression getFilterCondition() {
        return getOptionalFilterCondition().get();
    }

    @Override
    public FilterNode changeFilterCondition(ImmutableExpression newFilterCondition) {
        return new FilterNodeImpl(newFilterCondition);
    }

    @Override
    public SubstitutionResults<FilterNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) {
        return applyDescendingSubstitution(substitution, query);
    }

    @Override
    public SubstitutionResults<FilterNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            IntermediateQuery query) {
        Evaluation evaluation = transformBooleanExpression(query, substitution, getFilterCondition());

        /**
         * The condition cannot be satisfied --> the sub-tree is empty.
         */
        if (evaluation.isFalse()) {
            return new SubstitutionResultsImpl<>(DECLARE_AS_EMPTY);
        }
        else {
            /**
             * Propagates the substitution and ...
             */
            return evaluation.getOptionalExpression()
                    /**
                     * Still a condition: returns a filter node with the new condition
                     */
                    .map(exp -> new SubstitutionResultsImpl<>(changeFilterCondition(exp), substitution))
                    /**
                     * No condition: the filter node is not needed anymore
                     */
                    .orElseGet(() -> new SubstitutionResultsImpl<>(substitution, Optional.empty()));
        }
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof FilterNode)
                && ((FilterNode) node).getFilterCondition().equals(this.getFilterCondition());
    }

    @Override
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
        return new NodeTransformationProposalImpl(DELETE, emptyChild.getProjectedVariables());
    }

    @Override
    public String toString() {
        return FILTER_NODE_STR + getOptionalFilterString();
    }
}
