package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.PushDownBooleanExpressionProposalImpl;
import java.util.Optional;
import java.util.stream.Stream;

import it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.*;
import it.unibz.inf.ontop.pivotalrepr.unfolding.ProjectedVariableExtractionTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import static it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getDepthFirstNextNode;
import static it.unibz.inf.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getNextNodeAndQuery;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;

/**
 * Extensible through hooks
 */
public class PushDownBooleanExpressionOptimizerImpl implements PushDownBooleanExpressionOptimizer {

    /**
     * High-level method
     */
    @Override
    public IntermediateQuery optimize(final IntermediateQuery initialQuery) {
        return pushDownExpressions(initialQuery);
    }

    /**
     *
     * Tries to optimize all the JoinOrFilterNodes, ONE BY ONE.
     * Navigates in a top-down fashion.
     *
     */
    private IntermediateQuery pushDownExpressions(final IntermediateQuery initialQuery) {
        // Non-final
        Optional<QueryNode> optionalCurrentNode = initialQuery.getFirstChild(initialQuery.getRootConstructionNode());

        // Non-final
        IntermediateQuery currentQuery = initialQuery;

        while (optionalCurrentNode.isPresent()) {
            final QueryNode currentNode = optionalCurrentNode.get();

            /**
             * InnerJoinNode, LeftJoinNode, FilterNode or some extensions
             */
            if (currentNode instanceof JoinOrFilterNode) {
                NextNodeAndQuery nextNodeAndQuery = optimizeJoinOrFilter(currentQuery, (JoinOrFilterNode) currentNode);
                optionalCurrentNode = nextNodeAndQuery.getOptionalNextNode();
                currentQuery = nextNodeAndQuery.getNextQuery();
            }
            else {
                optionalCurrentNode = getDepthFirstNextNode(currentQuery, currentNode);
            }
        }
        return currentQuery;
    }


    /**
     * Tries to optimize one JoinOrFilterNode.
     * Returns information for the continuing the navigation in the possibly new IntermediateQuery.
     *
     */
    private NextNodeAndQuery optimizeJoinOrFilter(IntermediateQuery currentQuery, JoinOrFilterNode currentNode) {

        /**
         * Tries to build a PushDownBooleanExpressionProposal for the current node
         */
        Optional<PushDownBooleanExpressionProposal> optionalProposal = makeProposal(
                currentQuery, currentNode);

        /**
         * Applies it and extracts from the results the necessary information
         * for continuing the navigation
         */
        if (optionalProposal.isPresent()) {
            PushDownBooleanExpressionProposal proposal = optionalProposal.get();

            // Applies the proposal and casts the results
            NodeCentricOptimizationResults<JoinOrFilterNode> results;
            try {
                results = currentQuery.applyProposal(proposal);
            } catch (EmptyQueryException e) {
                throw new IllegalStateException("Unexpected empty query exception while pushing down boolean expressions");
            }

            return getNextNodeAndQuery(results);
        }
        else {
            return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentNode), currentQuery);
        }
    }

    /**
     * Routing method
     */
    private Optional<PushDownBooleanExpressionProposal> makeProposal(
            IntermediateQuery currentQuery, JoinOrFilterNode focusNode) {

        /**
         * Commutative joins and filters
         */
        if (focusNode instanceof CommutativeJoinOrFilterNode) {
            return makeProposalForJoinOrFilterNode(currentQuery, focusNode, currentQuery.getChildren(focusNode));
        }
        /**
         * Left join: can only push its conditions on the right side
         */
        else if (focusNode instanceof LeftJoinNode) {
            return makeProposalForJoinOrFilterNode(currentQuery, focusNode,
                    ImmutableList.of(currentQuery.getChild(focusNode, RIGHT)
                            .orElseThrow(() -> new IllegalStateException("Was not excepting to find a " +
                                    "LJ without a right element"))));
        }
        /**
         * Other node (useful for extension)
         */
        else {
            return makeProposalUnexpectedFocusNode(currentQuery, focusNode);
        }
    }

    /**
     * Hook (useful for extensions)
     *
     * By default, does not make a proposal
     */
    protected Optional<PushDownBooleanExpressionProposal> makeProposalUnexpectedFocusNode(IntermediateQuery currentQuery,
                                                                                          JoinOrFilterNode focusNode) {
        return Optional.empty();
    }


    /**
     * providerNode: provides the boolean expressions
     */
    private Optional<PushDownBooleanExpressionProposal> makeProposalForJoinOrFilterNode(
            IntermediateQuery currentQuery, JoinOrFilterNode providerNode, ImmutableList<QueryNode> preSelectedChildren) {

        /**
         * If there is no boolean expression, no proposal
         */
        Optional<ImmutableExpression> optionalNestedExpression = providerNode.getOptionalFilterCondition();

        if (!optionalNestedExpression.isPresent()) {
            return Optional.empty();
        }

        /**
         * Decomposes the boolean expressions as much as possible (conjunction)
         */
        ImmutableSet<ImmutableExpression> booleanExpressions = optionalNestedExpression.get().flattenAND();

        /**
         * For each boolean expression, looks for recipient nodes.
         *
         */
        ImmutableMultimap.Builder<QueryNode, ImmutableExpression> transferMapBuilder = ImmutableMultimap.builder();
        ImmutableList.Builder<ImmutableExpression> toKeepExpressionBuilder = ImmutableList.builder();

        for (ImmutableExpression expression : booleanExpressions) {

            ImmutableSet<QueryNode> recipients = preSelectedChildren.stream()
                    .flatMap(child -> selectRecipients(currentQuery, expression, providerNode, child))
                    .collect(ImmutableCollectors.toSet());

            if (recipients.isEmpty()) {
                throw new IllegalStateException("The expression should not disappear");
            }

            for (QueryNode recipientNode : recipients) {
                if (recipientNode != providerNode) {
                    transferMapBuilder.put(recipientNode, expression);
                }
                else {
                    toKeepExpressionBuilder.add(expression);
                }
            }

        }
        return buildProposal(providerNode, transferMapBuilder.build(), toKeepExpressionBuilder.build());
    }

    /**
     * TODO: explain
     */
    private Stream<QueryNode> selectRecipients(IntermediateQuery currentQuery,
                                               ImmutableExpression expression,
                                               JoinOrFilterNode providerNode, QueryNode currentNode) {
        if (currentNode instanceof CommutativeJoinOrFilterNode) {
            return selectRecipientsCommutativeJoinOrFilter(currentQuery, expression, providerNode,
                    (CommutativeJoinOrFilterNode) currentNode);
        }
        else if (currentNode instanceof LeftJoinNode) {
            return selectRecipientsLeftJoin(currentQuery, expression, providerNode, (LeftJoinNode) currentNode);
        }
        else if (currentNode instanceof DataNode) {
            return selectRecipientsDataNode(currentQuery, expression, providerNode, (DataNode) currentNode);
        }
        else if (currentNode instanceof ConstructionNode) {
            return selectRecipientsConstructionNode(currentQuery, expression, providerNode,
                    (ConstructionNode) currentNode);
        }
        else if (currentNode instanceof UnionNode) {
            return selectRecipientsUnionNode(currentQuery, expression, providerNode, (UnionNode) currentNode);
        }
        /**
         * Default
         */
        else {
            return selectRecipientsUnexceptedNode(currentQuery, expression, providerNode, currentNode);
        }
    }

    private Stream<QueryNode> selectRecipientsCommutativeJoinOrFilter(IntermediateQuery query,
                                                                      ImmutableExpression expression,
                                                                      JoinOrFilterNode providerNode,
                                                                      CommutativeJoinOrFilterNode currentNode) {
        ImmutableSet<Variable> expressionVariables = expression.getVariables();

        ImmutableSet<Variable> projectedVariables = ProjectedVariableExtractionTools.extractProjectedVariables(query,
                currentNode);

        /**
         * All the expression variables are projected: the current node is the recipient
         */
        if (projectedVariables.containsAll(expressionVariables)) {
            return Stream.of(currentNode);
        }
        /**
         * One part of the expression variables are projected: the expression must remain at the provider level
         */
        else if (expressionVariables.stream().anyMatch(projectedVariables::contains)) {
            return Stream.of(providerNode);
        }
        /**
         * This data node is independent of the expression.
         */
        else {
            return Stream.empty();
        }
    }

    private Stream<QueryNode> selectRecipientsLeftJoin(IntermediateQuery currentQuery,
                                                       ImmutableExpression expression,
                                                       JoinOrFilterNode providerNode,
                                                       LeftJoinNode currentNode) {
        throw new RuntimeException("TODO: implement LJ");
    }

    private Stream<QueryNode> selectRecipientsDataNode(IntermediateQuery query, ImmutableExpression expression,
                                                       JoinOrFilterNode providerNode, DataNode currentNode) {

        ImmutableSet<Variable> expressionVariables = expression.getVariables();

        ImmutableSet<Variable> projectedVariables = currentNode.getVariables();

        /**
         * All the expression variables are projected: the data node can be the recipient (if relevant)
         */
        if (projectedVariables.containsAll(expressionVariables)) {

            if (query.getParent(currentNode)
                    .filter(p -> p == providerNode)
                    .isPresent()) {
                return Stream.of(providerNode);
            }
            else {
                return Stream.of(currentNode);
            }
        }
        /**
         * One part of the expression variables are projected: the expression must remain at the provider level
         */
        else if (expressionVariables.stream().anyMatch(projectedVariables::contains)) {
            return Stream.of(providerNode);
        }
        /**
         * This data node is independent of the expression.
         */
        else {
            return Stream.empty();
        }
    }

    private Stream<QueryNode> selectRecipientsConstructionNode(IntermediateQuery currentQuery,
                                                               ImmutableExpression expression,
                                                               JoinOrFilterNode providerNode,
                                                               ConstructionNode currentNode) {
        throw new RuntimeException("TODO: implement construction node");
    }

    private Stream<QueryNode> selectRecipientsUnionNode(IntermediateQuery currentQuery, ImmutableExpression expression,
                                                        JoinOrFilterNode providerNode, UnionNode currentNode) {
        throw new RuntimeException("TODO: implement union node");
    }

    /**
     * Hook
     *
     * By default, does not push down (no optimization)
     */
    protected Stream<QueryNode> selectRecipientsUnexceptedNode(IntermediateQuery currentQuery,
                                                               ImmutableExpression expression,
                                                               JoinOrFilterNode providerNode, QueryNode currentNode) {
        return Stream.of(providerNode);
    }

    /**
     * Builds the PushDownBooleanExpressionProposal.
     */
    private Optional<PushDownBooleanExpressionProposal> buildProposal(
            JoinOrFilterNode focusNode, ImmutableMultimap<QueryNode, ImmutableExpression> transferMap,
            ImmutableList<ImmutableExpression> toKeepExpressions) {
        if (transferMap.isEmpty()) {
            return Optional.empty();
        }
        else {
            PushDownBooleanExpressionProposal proposal = new PushDownBooleanExpressionProposalImpl(
                    focusNode, transferMap, toKeepExpressions);
            return Optional.of(proposal);
        }
    }

}
