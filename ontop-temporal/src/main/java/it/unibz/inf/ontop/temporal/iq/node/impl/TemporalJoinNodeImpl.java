package it.unibz.inf.ontop.temporal.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.QueryNodeSubstitutionException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.SubstitutionResultsImpl;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.JoinLikeNodeImpl;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermConstants;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.temporal.iq.node.TemporalJoinNode;
import it.unibz.inf.ontop.temporal.iq.node.TemporalQueryNodeVisitor;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;

import static it.unibz.inf.ontop.iq.node.SubstitutionResults.LocalAction.DECLARE_AS_EMPTY;
import static it.unibz.inf.ontop.iq.node.SubstitutionResults.LocalAction.NO_CHANGE;

public class TemporalJoinNodeImpl extends JoinLikeNodeImpl implements TemporalJoinNode {

    private static final String JOIN_NODE_STR = "TEMPORALJOIN" ;

    @AssistedInject
    protected TemporalJoinNodeImpl(@Assisted Optional<ImmutableExpression> optionalFilterCondition,
                                TermNullabilityEvaluator nullabilityEvaluator) {
        super(optionalFilterCondition, nullabilityEvaluator);
    }

    @AssistedInject
    private TemporalJoinNodeImpl(@Assisted ImmutableExpression joiningCondition,
                              TermNullabilityEvaluator nullabilityEvaluator) {
        super(Optional.of(joiningCondition), nullabilityEvaluator);
    }

    @AssistedInject
    private TemporalJoinNodeImpl(TermNullabilityEvaluator nullabilityEvaluator) {
        super(Optional.empty(), nullabilityEvaluator);
    }

    @Override
    public TemporalJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition) {
        return new TemporalJoinNodeImpl(newOptionalFilterCondition, getNullabilityEvaluator());
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        ((TemporalQueryNodeVisitor)visitor).visit(this);
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
    public SubstitutionResults<TemporalJoinNode> applyAscendingSubstitution
            (ImmutableSubstitution<? extends ImmutableTerm> substitution, QueryNode childNode, IntermediateQuery query)
            throws QueryNodeSubstitutionException {
        if (substitution.isEmpty()) {
            return new SubstitutionResultsImpl<>(NO_CHANGE);
        }

        ImmutableSet<Variable> nullVariables = substitution.getImmutableMap().entrySet().stream()
                .filter(e -> e.getValue().equals(TermConstants.NULL))
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

    private SubstitutionResults<TemporalJoinNode> applyEvaluation(ExpressionEvaluator.EvaluationResult evaluationResult,
                                                               ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        if (evaluationResult.isEffectiveFalse()) {
            return new SubstitutionResultsImpl<>(DECLARE_AS_EMPTY);
        }
        else {
            TemporalJoinNode newNode = changeOptionalFilterCondition(evaluationResult.getOptionalExpression());
            return new SubstitutionResultsImpl<>(newNode, substitution);
        }
    }

    @Override
    public SubstitutionResults<TemporalJoinNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) throws QueryNodeSubstitutionException {
        return getOptionalFilterCondition()
                .map(cond -> transformBooleanExpression(substitution, cond))
                .map(ev -> applyEvaluation(ev, substitution))
                .orElseGet(() -> new SubstitutionResultsImpl<>(NO_CHANGE, Optional.of(substitution)));
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
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
        return null;
    }

    @Override
    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueNode) {
        return null;
    }

    @Override
    public String toString() {
        return JOIN_NODE_STR + getOptionalFilterString();
    }
}
