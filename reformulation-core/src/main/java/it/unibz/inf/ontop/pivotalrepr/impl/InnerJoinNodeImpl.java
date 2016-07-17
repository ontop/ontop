package it.unibz.inf.ontop.pivotalrepr.impl;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import static it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionTools.computeNullSubstitution;
import static it.unibz.inf.ontop.pivotalrepr.NodeTransformationProposedState.*;
import static it.unibz.inf.ontop.pivotalrepr.NodeTransformationProposedState.REPLACE_BY_NEW_NODE;
import static it.unibz.inf.ontop.pivotalrepr.SubstitutionResults.LocalAction.DECLARE_AS_EMPTY;
import static it.unibz.inf.ontop.pivotalrepr.SubstitutionResults.LocalAction.NO_CHANGE;

public class InnerJoinNodeImpl extends JoinLikeNodeImpl implements InnerJoinNode {

    private static final String JOIN_NODE_STR = "JOIN" ;

    public InnerJoinNodeImpl(Optional<ImmutableExpression> optionalFilterCondition) {
        super(optionalFilterCondition);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public InnerJoinNode clone() {
        return new InnerJoinNodeImpl(getOptionalFilterCondition());
    }

    @Override
    public InnerJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public InnerJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition) {
        return new InnerJoinNodeImpl(newOptionalFilterCondition);
    }

    @Override
    public SubstitutionResults<InnerJoinNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) {

        if (substitution.isEmpty()) {
            return new SubstitutionResultsImpl<>(NO_CHANGE);
        }

        ImmutableSet<Variable> nullVariables = substitution.getImmutableMap().entrySet().stream()
                .filter(e -> e.getValue().equals(OBDAVocabulary.NULL))
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());


        ImmutableSet<Variable > otherNodesProjectedVariables = query.getOtherChildrenStream(this, childNode)
                .flatMap(c -> query.getProjectedVariables(c).stream())
                .collect(ImmutableCollectors.toSet());

        /**
         * If there is an implicit equality involving one null variables, the join is empty.
         */
        if (otherNodesProjectedVariables.stream()
                .anyMatch(nullVariables::contains)) {
            // Reject
            return new SubstitutionResultsImpl<>(DECLARE_AS_EMPTY);
        }

        return computeAndEvaluateNewCondition(substitution, query)
                .map(ev -> applyEvaluation(ev, substitution))
                .orElseGet(() -> new SubstitutionResultsImpl<>(NO_CHANGE, Optional.of(substitution)));
    }

    @Override
    public SubstitutionResults<InnerJoinNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {

        return getOptionalFilterCondition()
                .map(cond -> transformBooleanExpression(query, substitution, cond))
                .map(ev -> applyEvaluation(ev, substitution))
                .orElseGet(() -> new SubstitutionResultsImpl<>(NO_CHANGE, Optional.of(substitution)));
    }

    private SubstitutionResults<InnerJoinNode> applyEvaluation(ExpressionEvaluator.Evaluation evaluation,
                                                               ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        if (evaluation.isFalse()) {
            return new SubstitutionResultsImpl<>(DECLARE_AS_EMPTY);
        }
        else {
            InnerJoinNode newNode = changeOptionalFilterCondition(evaluation.getOptionalExpression());
            return new SubstitutionResultsImpl<>(newNode, substitution);
        }
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof InnerJoinNode) &&
            this.getOptionalFilterCondition().equals(((InnerJoinNode) node).getOptionalFilterCondition());
    }

    @Override
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
        ImmutableList<QueryNode> remainingChildren = query.getChildrenStream(this)
                .filter(c -> c != emptyChild)
                .collect(ImmutableCollectors.toList());

        ImmutableSet<Variable> variablesProjectedByDeletedChild = emptyChild.getProjectedVariables();
        ImmutableSet<Variable> otherNodesProjectedVariables = query.getProjectedVariables(this);

        /**
         * If there is an implicit equality involving one null variables, the join is empty.
         */
        if (otherNodesProjectedVariables.stream()
                .anyMatch(variablesProjectedByDeletedChild::contains)) {
            return rejectInnerJoin(otherNodesProjectedVariables, variablesProjectedByDeletedChild);
        }

        Optional<ImmutableExpression> formerCondition = getOptionalFilterCondition();

        Optional<ExpressionEvaluator.Evaluation> optionalEvaluation = formerCondition
                .map(cond -> computeNullSubstitution(variablesProjectedByDeletedChild).applyToBooleanExpression(cond))
                .map(cond -> new ExpressionEvaluator(query.getMetadata().getUriTemplateMatcher())
                        .evaluateExpression(cond));

        /**
         * The new condition is not satisfied anymore
         */
        if (optionalEvaluation
                .filter(ExpressionEvaluator.Evaluation::isFalse)
                .isPresent()) {
            // Reject
            return rejectInnerJoin(otherNodesProjectedVariables, variablesProjectedByDeletedChild);
        }
        /**
         * The condition still holds
         */
        else {
            Optional<ImmutableExpression> newCondition = optionalEvaluation
                    .flatMap(ExpressionEvaluator.Evaluation::getOptionalExpression);

            switch (remainingChildren.size()) {
                case 0:
                    return new NodeTransformationProposalImpl(NodeTransformationProposedState.DECLARE_AS_EMPTY,
                            variablesProjectedByDeletedChild);
                case 1:
                    if (newCondition.isPresent()) {
                        return new NodeTransformationProposalImpl(REPLACE_BY_NEW_NODE,
                                new FilterNodeImpl(newCondition.get()),
                                variablesProjectedByDeletedChild);
                    } else {
                        return new NodeTransformationProposalImpl(REPLACE_BY_UNIQUE_NON_EMPTY_CHILD, remainingChildren.get(0),
                                variablesProjectedByDeletedChild);
                    }
                default:
                    if (newCondition.equals(formerCondition)) {
                        return new NodeTransformationProposalImpl(NO_LOCAL_CHANGE, variablesProjectedByDeletedChild);
                    } else {
                        return new NodeTransformationProposalImpl(REPLACE_BY_NEW_NODE,
                                new InnerJoinNodeImpl(newCondition),
                                variablesProjectedByDeletedChild);
                    }
            }
        }
    }

    private NodeTransformationProposal rejectInnerJoin(ImmutableSet<Variable> otherNodesProjectedVariables,
                                                       ImmutableSet<Variable> variablesProjectedByDeletedChild) {
        return new NodeTransformationProposalImpl(NodeTransformationProposedState.DECLARE_AS_EMPTY,
                union(otherNodesProjectedVariables, variablesProjectedByDeletedChild));
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        return JOIN_NODE_STR + getOptionalFilterString();
    }
}
