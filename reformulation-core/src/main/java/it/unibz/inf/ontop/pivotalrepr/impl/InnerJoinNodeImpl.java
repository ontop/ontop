package it.unibz.inf.ontop.pivotalrepr.impl;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.ImmutabilityTools;
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

        /**
         * If some variables are shared among the children, add an explicit equality in the join node
         */
        Optional<ImmutableExpression> additionalCondition = foldBooleanExpressions(
                substitution.getImmutableMap().entrySet().stream()
                    .filter(e -> otherNodesProjectedVariables.contains(e.getKey()))
                    .map(e -> DATA_FACTORY.getImmutableExpression(ExpressionOperation.EQ, e.getKey(), e.getValue())));

        Optional<ImmutableExpression> formerCondition = getOptionalFilterCondition();

        Optional<ImmutableExpression> newCondition = formerCondition
                .map(substitution::applyToBooleanExpression)
                // Combines the two possible conditions
                .map(cond -> additionalCondition
                        .map(addCond -> foldBooleanExpressions(cond,addCond))
                        .orElseGet(() -> Optional.of(cond)))
                .orElse(additionalCondition)
                .flatMap(cond -> new ExpressionEvaluator(query.getMetadata().getUriTemplateMatcher())
                        .evaluateExpression(cond));

        /**
         * The new condition is not satisfied anymore
         */
        if ((!newCondition.isPresent()) && formerCondition.isPresent()) {
            // Reject
            return new SubstitutionResultsImpl<>(IS_EMPTY);
        }
        else {
            InnerJoinNode newNode = new InnerJoinNodeImpl(newCondition);
            return new SubstitutionResultsImpl<>(newNode, substitution);
        }
    }

    @Override
    public SubstitutionResults<InnerJoinNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution) {

        Optional<ImmutableExpression> newOptionalCondition = transformOptionalBooleanExpression(substitution,
                getOptionalFilterCondition());
        InnerJoinNode newNode = new InnerJoinNodeImpl(newOptionalCondition);

        return new SubstitutionResultsImpl<>(newNode, substitution);
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
