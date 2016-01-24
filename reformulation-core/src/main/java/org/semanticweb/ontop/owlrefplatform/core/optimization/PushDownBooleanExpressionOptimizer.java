package org.semanticweb.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.PushDownBooleanExpressionProposalImpl;
import java.util.Optional;


import static org.semanticweb.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.*;
import static org.semanticweb.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getDepthFirstNextNode;

/**
 * Pushes immutable boolean expressions down into the intermediate query tree if possible.
 *
 * Current restriction: works for CommutativeJoins and Filters but not for Left-Joins yet.
 * TODO: also support LJs (should be quick straight-forward).
 *
 */
public class PushDownBooleanExpressionOptimizer implements IntermediateQueryOptimizer {

    /**
     * All the target nodes (that will "receive or keep" the boolean expression)
     * have to be associated to a delimiter node.
     */
    private static class DelimiterTargetPair {

        /**
         * Can be a construction node, a data node
         */
        public final SubTreeDelimiterNode delimiterNode;

        /**
         * In one special case (when the delimiter node is a data node), the target MAY BE also the source.
         *
         * Example: one JOIN followed by data nodes (delimiters) and  a construction node (also a delimiter).
         * This join will be target for expressions related to the variables of data nodes, while
         * the sub-query (the sub-tree of the construction node) will have its own target.
         *
         */
        public final QueryNode targetNode;

        private DelimiterTargetPair(SubTreeDelimiterNode delimiterNode, QueryNode targetNode) {
            this.delimiterNode = delimiterNode;
            this.targetNode = targetNode;
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
        if ((currentNode instanceof CommutativeJoinNode)
                || (currentNode instanceof FilterNode)) {
            return makeProposalForFilterOrCommutativeJoin(currentQuery, currentNode);
        }
        /**
         * Left-join is not yet supported
         */
        else {
            return Optional.empty();
        }
    }

    /**
     * Builds a proposal for a  Filter or CommutativeJoin node.
     *
     * NOT FOR LJs!
     */
    private Optional<PushDownBooleanExpressionProposal> makeProposalForFilterOrCommutativeJoin(
            IntermediateQuery currentQuery, JoinOrFilterNode currentNode) {

        if (currentNode instanceof LeftJoinNode) {
            throw new IllegalArgumentException("This method does not support LJs!");
        }

        /**
         * If there is no boolean expression, no proposal
         */
        Optional<ImmutableBooleanExpression> optionalNestedExpression = currentNode.getOptionalFilterCondition();

        if (!optionalNestedExpression.isPresent()) {
            return Optional.empty();
        }

        /**
         * Decomposes the boolean expressions as much as possible (conjunction)
         */
        ImmutableSet<ImmutableBooleanExpression> booleanExpressions = optionalNestedExpression.get().flattenAND();

        /**
         * Finds a DelimiterTargetPair for each child sub-tree of the current node
         */
        ImmutableList<DelimiterTargetPair> potentialTargetNodes = findCandidateTargetNodes(currentQuery, currentNode);

        /**
         * For each boolean expression, looks for target nodes.
         *
         * Deals with the special cases where the source node is also a target node
         * for some boolean expressions.
         *
         */
        ImmutableMultimap.Builder<QueryNode, ImmutableBooleanExpression> transferMapBuilder = ImmutableMultimap.builder();
        ImmutableList.Builder<ImmutableBooleanExpression> toKeepExpressionBuilder = ImmutableList.builder();

        for (ImmutableBooleanExpression expression : booleanExpressions) {
            ImmutableList<QueryNode> nodesForTransfer = selectNodesForTransfer(expression, potentialTargetNodes);

            /**
             * If a delimiter node is a DataNode, the source node may also be a target.
             * Non-final.
             */
            boolean isSourceAlsoTarget = false;

            for (QueryNode targetNode : nodesForTransfer) {
                if (targetNode != currentNode)
                    transferMapBuilder.put(targetNode, expression);
                else
                    isSourceAlsoTarget = true;
            }
            /**
             * If no transfer or if the source node is also a target
             * marks the expression as to be kept in the source node.
             */
            if (isSourceAlsoTarget || nodesForTransfer.isEmpty()) {
                toKeepExpressionBuilder.add(expression);
            }
        }
        return buildProposal(currentNode, transferMapBuilder.build(), toKeepExpressionBuilder.build());
    }


    /**
     * Finds at least one DelimiterTargetPair per sub-tree of a child of the source node.
     *
     * Top method.
     */
    private ImmutableList<DelimiterTargetPair> findCandidateTargetNodes(IntermediateQuery currentQuery,
                                                                        JoinOrFilterNode sourceNode) {

        ImmutableList.Builder<DelimiterTargetPair> candidateListBuilder = ImmutableList.builder();
        for (QueryNode childNode : currentQuery.getChildren(sourceNode)) {
            candidateListBuilder.addAll(
                    findCandidatesInSubTree(currentQuery, childNode, sourceNode, Optional.<SubTreeDelimiterNode>empty())
            );
        }
        return candidateListBuilder.build();
    }

    /**
     * Recursive low-level method.
     */
    private ImmutableList<DelimiterTargetPair> findCandidatesInSubTree(IntermediateQuery currentQuery, QueryNode currentNode,
                                                                       JoinOrFilterNode sourceNode,
                                                                       final Optional<SubTreeDelimiterNode> optionalClosestDelimiterNode) {

        /**
         * First terminal case: a data node
         */
        if (currentNode instanceof DataNode) {

            /**
             * If there is at least one delimiter node between the source and the current node,
             * returns this data node as the target and the delimiter node as delimiter.
             */
            if (optionalClosestDelimiterNode.isPresent()) {
                return ImmutableList.of(new DelimiterTargetPair(optionalClosestDelimiterNode.get(), currentNode));
            }
            /**
             * Otherwise, the DATA NODE HAS THE ROLE OF DELIMITER and the SOURCE NODE is used as target.
             */
            else {
                return ImmutableList.of(new DelimiterTargetPair((DataNode)currentNode, sourceNode));
            }
        }

        /**
         * Second terminal case: a JoinOrFilter found after a delimiter node.
         *
         * The JoinOrFilter is then used as a target and the delimiter node as a delimiter.
         *
         */
        if ((currentNode instanceof JoinOrFilterNode) && optionalClosestDelimiterNode.isPresent()) {
            return ImmutableList.of(new DelimiterTargetPair(optionalClosestDelimiterNode.get(), currentNode));
        }

        /**
         * Special case: some delimiter nodes are ALSO BE COMMUTATIVE JOINS
         * (in some extensions of Ontop)
         *
         * Then these special nodes are both used as targets and delimiters.
         *
         * Returns a pair.
         *
         */
        if (currentNode instanceof DelimiterCommutativeJoinNode) {
            return ImmutableList.of(new DelimiterTargetPair((SubTreeDelimiterNode)currentNode, currentNode));
        }


        Optional<SubTreeDelimiterNode> newOptionalClosestDelimiterNode;

        /**
         * If is a normal delimiter node, uses it as the closest one
         */
        if (currentNode instanceof SubTreeDelimiterNode) {
            SubTreeDelimiterNode delimiterNode = (SubTreeDelimiterNode) currentNode;
            newOptionalClosestDelimiterNode = Optional.of(delimiterNode);

        }
        /**
         * However, reuses the previous one (if existing).
         */
        else {
            newOptionalClosestDelimiterNode = optionalClosestDelimiterNode;
        }

        /**
         * Gathers the pairs returned by the children and returns them.
         *
         * Recursive.
         */
        ImmutableList.Builder<DelimiterTargetPair> candidateListBuilder = ImmutableList.builder();
        for (QueryNode child : currentQuery.getChildren(currentNode)) {
            // Recursive call
            candidateListBuilder.addAll(
                    findCandidatesInSubTree(currentQuery, child, sourceNode, newOptionalClosestDelimiterNode)
            );
        }
        return candidateListBuilder.build();
    }

    /**
     * Selects target nodes for one boolean expression.
     *
     * Criterion: the delimiter node should contain all the variables used in the boolean expression.
     */
    private ImmutableList<QueryNode> selectNodesForTransfer(ImmutableBooleanExpression expression,
                                                            ImmutableList<DelimiterTargetPair> potentialTargetPairs) {
        ImmutableList.Builder<QueryNode> selectionBuilder = ImmutableList.builder();
        for (DelimiterTargetPair pair : potentialTargetPairs) {
            ImmutableSet<Variable> expressionVariables = expression.getVariables();
            ImmutableSet<Variable> delimiterVariables = pair.delimiterNode.getProjectionAtom().getVariables();

            if (delimiterVariables.containsAll(expressionVariables)) {
                selectionBuilder.add(pair.targetNode);
            }
        }

        return selectionBuilder.build();
    }

    /**
     * Builds the PushDownBooleanExpressionProposal.
     */
    private Optional<PushDownBooleanExpressionProposal> buildProposal(
            JoinOrFilterNode focusNode, ImmutableMultimap<QueryNode, ImmutableBooleanExpression> transferMap,
            ImmutableList<ImmutableBooleanExpression> toKeepExpressions) {
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
