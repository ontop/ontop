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
 * Pushes immutable boolean expressions down into the intermediate query tree if possible.
 *
 * Current restriction: works for CommutativeJoins and Filters but not for Left-Joins yet.
 * TODO: also support LJs (should be quick straight-forward).
 *
 * TODO: refactor
 *
 */
public class PushDownBooleanExpressionOptimizer implements IntermediateQueryOptimizer {

    /**
     * All the target nodes (that will "receive or keep" the boolean expression)
     * have to be associated to a delimiter node.
     */
    private static class PotentialRecipient {

        /**
         * Node that can directly receive boolean expressions (JoinOrFilter node)
         * or can insert a new FilterNode to receive it instead of itself (data node).
         *
         */
        public final QueryNode recipientNode;

        /**
         * Variables of the tuples produced by the recipient node
         */
        public final ImmutableSet<Variable> projectedVariables;

        private PotentialRecipient(QueryNode recipientNode, ImmutableSet<Variable> projectedVariables) {
            if (!((recipientNode instanceof DataNode )||
                    recipientNode instanceof JoinOrFilterNode)) {
                throw new IllegalArgumentException("Illegal recipient node: " + recipientNode);
            }

            this.recipientNode = recipientNode;
            this.projectedVariables = projectedVariables;
        }
    }

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
            IntermediateQuery currentQuery, JoinOrFilterNode currentNode) {

        /**
         * Commutative joins and filters
         */
        if (currentNode instanceof CommutativeJoinOrFilterNode) {
            return makeProposalForJoinOrFilterNode(currentQuery, currentNode, currentQuery.getChildren(currentNode));
        }
        /**
         * Left join: can only push its conditions on the right side
         */
        else if (currentNode instanceof LeftJoinNode) {
            return makeProposalForJoinOrFilterNode(currentQuery, currentNode,
                    ImmutableList.of(currentQuery.getChild(currentNode, RIGHT)
                            .orElseThrow(() -> new IllegalStateException("Was not excepting to find a " +
                                    "LJ without a right element"))));
        }
        /**
         * Does not optimize other nodes
         */
        else {
            return Optional.empty();
        }
    }

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

            ImmutableList<QueryNode> nodesForTransfer = selectNodesForTransfer(currentQuery, expression, providerNode,
                    preSelectedChildren);


            /**
             * Becomes true if the provider is a recipient
             */
            boolean isProviderAlsoRecipient = false;

            for (QueryNode recipientNode : nodesForTransfer) {
                if (recipientNode != providerNode)
                    transferMapBuilder.put(recipientNode, expression);
                else
                    isProviderAlsoRecipient = true;
            }
            /**
             * If no transfer or the provider is a recipient
             * marks the expression as to be kept in the provider node.
             */
            if (isProviderAlsoRecipient || nodesForTransfer.isEmpty()) {
                toKeepExpressionBuilder.add(expression);
            }
        }
        return buildProposal(providerNode, transferMapBuilder.build(), toKeepExpressionBuilder.build());
    }

    private ImmutableList<QueryNode> selectNodesForTransfer(IntermediateQuery currentQuery, ImmutableExpression expression,
                                                            JoinOrFilterNode providerNode,
                                                            ImmutableList<QueryNode> preSelectedChildren) {
        return preSelectedChildren.stream()
                .flatMap(child -> selectNodesForTransferInSubTree(currentQuery, expression, providerNode, child))
                .collect(ImmutableCollectors.toList());
    }

    private Stream<QueryNode> selectNodesForTransferInSubTree(IntermediateQuery currentQuery,
                                                              ImmutableExpression expression,
                                                              JoinOrFilterNode providerNode, QueryNode currentNode) {
        if (currentNode instanceof CommutativeJoinOrFilterNode) {
            throw new RuntimeException("TODO: implement");
        }
        else if (currentNode instanceof LeftJoinNode) {
            throw new RuntimeException("TODO: implement");
        }
        else if (currentNode instanceof DataNode) {
            throw new RuntimeException("TODO: implement");
        }
        else if (currentNode instanceof ConstructionNode) {
            throw new RuntimeException("TODO: implement");
        }
        else if (currentNode instanceof UnionNode) {
            throw new RuntimeException("TODO: implement");
        }
        /**
         * By default, does not push down (no optimization)
         */
        else {
            return Stream.of(providerNode);
        }
    }

//
//    /**
//     * Finds at least one PotentialRecipient per sub-tree of a child of the source node.
//     *
//     * Top method.
//     */
//    private ImmutableList<PotentialRecipient> findCandidateRecipients(IntermediateQuery currentQuery,
//                                                                      JoinOrFilterNode sourceNode) {
//
//        ImmutableList.Builder<PotentialRecipient> candidateListBuilder = ImmutableList.builder();
//        for (QueryNode childNode : currentQuery.getChildren(sourceNode)) {
//            candidateListBuilder.addAll(
//                    findCandidatesInSubTree(currentQuery, childNode, sourceNode, Optional.<ConstructionOrDataNode>empty())
//            );
//        }
//        return candidateListBuilder.build();
//    }
//
//    /**
//     * Recursive low-level method.
//     */
//    private ImmutableList<PotentialRecipient> findCandidatesInSubTree(IntermediateQuery currentQuery, QueryNode currentNode,
//                                                                      JoinOrFilterNode sourceNode,
//                                                                      final Optional<ConstructionOrDataNode> optionalClosestDelimiterNode) {
//
//        /**
//         * First terminal case: a data node
//         */
//        if (currentNode instanceof DataNode) {
//
//            /**
//             * If there is at least one delimiter node between the source and the current node,
//             * returns this data node as the target and the delimiter node as delimiter.
//             */
//            if (optionalClosestDelimiterNode.isPresent()) {
//                return ImmutableList.of(new PotentialRecipient(optionalClosestDelimiterNode.get(), currentNode));
//            }
//            /**
//             * Otherwise, the DATA NODE HAS THE ROLE OF DELIMITER and the SOURCE NODE is used as target.
//             */
//            else {
//                return ImmutableList.of(new PotentialRecipient((DataNode)currentNode, sourceNode));
//            }
//        }
//
//        /**
//         * Second terminal case: a JoinOrFilter found after a delimiter node.
//         *
//         * The JoinOrFilter is then used as a target and the delimiter node as a delimiter.
//         *
//         */
//        else if ((currentNode instanceof JoinOrFilterNode) && optionalClosestDelimiterNode.isPresent()) {
//            return ImmutableList.of(new PotentialRecipient(optionalClosestDelimiterNode.get(), currentNode));
//        }
//
//        Optional<ConstructionOrDataNode> newOptionalClosestDelimiterNode;
//
//        /**
//         * If is a normal delimiter node, uses it as the closest one
//         */
//        if (currentNode instanceof ConstructionNode) {
//            ConstructionOrDataNode delimiterNode = (ConstructionNode) currentNode;
//            newOptionalClosestDelimiterNode = Optional.of(delimiterNode);
//
//        }
//        /**
//         * However, reuses the previous one (if existing).
//         */
//        else {
//            newOptionalClosestDelimiterNode = optionalClosestDelimiterNode;
//        }
//
//        /**
//         * Gathers the pairs returned by the children and returns them.
//         *
//         * Recursive.
//         */
//        ImmutableList.Builder<PotentialRecipient> candidateListBuilder = ImmutableList.builder();
//        for (QueryNode child : currentQuery.getChildren(currentNode)) {
//            // Recursive call
//            candidateListBuilder.addAll(
//                    findCandidatesInSubTree(currentQuery, child, sourceNode, newOptionalClosestDelimiterNode)
//            );
//        }
//        return candidateListBuilder.build();
//    }
//
//    /**
//     * Selects target nodes for one boolean expression.
//     *
//     * Criterion: the delimiter node should contain all the variables used in the boolean expression.
//     */
//    private ImmutableList<QueryNode> selectNodesForTransfer(IntermediateQuery query, ImmutableExpression expression,
//                                                            QueryNode currentNode) {
//        ImmutableList.Builder<QueryNode> selectionBuilder = ImmutableList.builder();
//        for (PotentialRecipient pair : potentialTargetPairs) {
//            ImmutableSet<Variable> expressionVariables = expression.getVariables();
//            ImmutableSet<Variable> delimiterVariables = ProjectedVariableExtractionTools.extractProjectedVariables(
//                    query, pair.delimiterNode);
//
//            if (delimiterVariables.containsAll(expressionVariables)) {
//                selectionBuilder.add(pair.recipientNode);
//            }
//        }
//
//        return selectionBuilder.build();
//    }

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
