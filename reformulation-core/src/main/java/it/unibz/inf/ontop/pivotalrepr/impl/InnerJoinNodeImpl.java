package it.unibz.inf.ontop.pivotalrepr.impl;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import static it.unibz.inf.ontop.model.impl.ImmutabilityTools.foldBooleanExpressions;
import static it.unibz.inf.ontop.pivotalrepr.unfolding.ProjectedVariableExtractionTools.extractProjectedVariables;

public class InnerJoinNodeImpl extends JoinLikeNodeImpl implements InnerJoinNode {

    private static final String JOIN_NODE_STR = "JOIN" ;
    private static final boolean IS_EMPTY = true;
    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

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
            QueryNode descendantNode, IntermediateQuery query) {

        if (substitution.isEmpty()) {
            return new SubstitutionResultsImpl<>(this);
        }

        ImmutableSet<Variable> nullVariables = substitution.getImmutableMap().entrySet().stream()
                .filter(e -> e.getValue().equals(OBDAVocabulary.NULL))
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());


        ImmutableSet<Variable > otherNodesProjectedVariables = query.getChildren(this).stream()
                .filter(c -> c != descendantNode)
                .flatMap(c -> extractProjectedVariables(query, c).stream())
                .collect(ImmutableCollectors.toSet());

        /**
         * If there is an implicit equality involving one null variables, the join is empty.
         */
        if (otherNodesProjectedVariables.stream()
                .anyMatch(nullVariables::contains)) {
            // Reject
            return new SubstitutionResultsImpl<>(IS_EMPTY);
        }

        return computeAndEvaluateNewCondition(substitution, query, otherNodesProjectedVariables)
                .map(ev -> applyEvaluation(ev, substitution))
                .orElseGet(() -> new SubstitutionResultsImpl<>(this, substitution));
    }

    @Override
    public SubstitutionResults<InnerJoinNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {

        return getOptionalFilterCondition()
                .map(cond -> transformBooleanExpression(query, substitution, cond))
                .map(ev -> applyEvaluation(ev, substitution))
                .orElseGet(() -> new SubstitutionResultsImpl<>(this, substitution));
    }

    private SubstitutionResults<InnerJoinNode> applyEvaluation(ExpressionEvaluator.Evaluation evaluation,
                                                               ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        if (evaluation.isFalse()) {
            return new SubstitutionResultsImpl<>(IS_EMPTY);
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
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        return JOIN_NODE_STR + getOptionalFilterString();
    }
}
