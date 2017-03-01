package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.NodeTransformationProposedState.*;
import static it.unibz.inf.ontop.pivotalrepr.SubstitutionResults.LocalAction.DECLARE_AS_EMPTY;
import static it.unibz.inf.ontop.pivotalrepr.SubstitutionResults.LocalAction.NO_CHANGE;

public class InnerJoinNodeImpl extends JoinLikeNodeImpl implements InnerJoinNode {

    private static final String JOIN_NODE_STR = "JOIN" ;

    @AssistedInject
    protected InnerJoinNodeImpl(@Assisted Optional<ImmutableExpression> optionalFilterCondition) {
        super(optionalFilterCondition);
    }

    @AssistedInject
    private InnerJoinNodeImpl(@Assisted ImmutableExpression joiningCondition) {
        super(Optional.of(joiningCondition));
    }

    @AssistedInject
    private InnerJoinNodeImpl() {
        super(Optional.empty());
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
                .flatMap(c -> query.getVariables(c).stream())
                .collect(ImmutableCollectors.toSet());

        /**
         * If there is an implicit equality involving one null variables, the join is empty.
         */
        if (otherNodesProjectedVariables.stream()
                .anyMatch(nullVariables::contains)) {
            // Reject
            return new SubstitutionResultsImpl<>(DECLARE_AS_EMPTY);
        }

        return computeAndEvaluateNewCondition(substitution, Optional.empty())
                .map(ev -> applyEvaluation(ev, substitution))
                .orElseGet(() -> new SubstitutionResultsImpl<>(NO_CHANGE, Optional.of(substitution)));
    }

    @Override
    public SubstitutionResults<InnerJoinNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {

        return getOptionalFilterCondition()
                .map(cond -> transformBooleanExpression(substitution, cond))
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

        return new NodeTransformationProposalImpl(NodeTransformationProposedState.DECLARE_AS_EMPTY,
                query.getVariables(this));
    }

    @Override
    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueChild) {
        ImmutableList<QueryNode> remainingChildren = query.getChildrenStream(this)
                .filter(c -> c != trueChild)
                .collect(ImmutableCollectors.toList());
        switch (remainingChildren.size()) {
            case 0:
                return new NodeTransformationProposalImpl(DECLARE_AS_TRUE,
                        ImmutableSet.of());
            case 1:
                Optional<ImmutableExpression> condition = getOptionalFilterCondition();
                if (condition.isPresent()) {
                    return new NodeTransformationProposalImpl(
                            REPLACE_BY_NEW_NODE,
                            query.getFactory().createFilterNode(condition.get()),
                            ImmutableSet.of()
                    );
                }
                return new NodeTransformationProposalImpl(REPLACE_BY_UNIQUE_NON_EMPTY_CHILD,
                            remainingChildren.get(0),ImmutableSet.of());
            default:
                return new NodeTransformationProposalImpl(NO_LOCAL_CHANGE, ImmutableSet.of());
        }
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
