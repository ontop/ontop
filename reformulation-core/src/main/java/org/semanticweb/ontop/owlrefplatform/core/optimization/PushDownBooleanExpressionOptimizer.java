package org.semanticweb.ontop.owlrefplatform.core.optimization;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.PushDownBooleanExpressionProposalImpl;


import static org.semanticweb.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.*;
import static org.semanticweb.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getDepthFirstNextNode;

/**
 * TODO: explain
 *
 * BIAS: only interested in propagating boolean expressions behind SubTreeDelimiterNode(s).
 *
 */
public class PushDownBooleanExpressionOptimizer implements IntermediateQueryOptimizer {

    /**
     * TODO: explain
     */
    private static class NotSupportedCaseException extends Exception {
    }

    /**
     * TODO: explain
     */
    private static class DelimiterTargetPair {
        public final SubTreeDelimiterNode delimiterNode;
        public final QueryNode targetNode;

        private DelimiterTargetPair(SubTreeDelimiterNode delimiterNode, QueryNode targetNode) {
            this.delimiterNode = delimiterNode;
            this.targetNode = targetNode;
        }
    }



    @Override
    public IntermediateQuery optimize(final IntermediateQuery initialQuery) {
        try {
            return pushDownExpressions(initialQuery);
        } catch (InvalidQueryOptimizationProposalException e) {
            throw new RuntimeException("TODO: unexpected exception: " + e.getMessage());
        }
    }

    /**
     * TODO: explain
     */
    private IntermediateQuery pushDownExpressions(final IntermediateQuery initialQuery)
            throws InvalidQueryOptimizationProposalException {
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
     * TODO: explain
     */
    private NextNodeAndQuery optimizeJoinOrFilter(IntermediateQuery currentQuery, JoinOrFilterNode currentNode)
            throws InvalidQueryOptimizationProposalException {
        Optional<PushDownBooleanExpressionProposal> optionalProposal = makeProposal(
                currentQuery, currentNode);

        if (optionalProposal.isPresent()) {
            PushDownBooleanExpressionProposal proposal = optionalProposal.get();

            // Applies the proposal and casts the results
            NodeCentricOptimizationResults<JoinOrFilterNode> results;
            try {
                results = proposal.castResults(
                        currentQuery.applyProposal(proposal));
            } catch (EmptyQueryException e) {
                throw new RuntimeException("Unexpected empty query exception while pushing down boolean expressions");
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
            return Optional.absent();
        }
    }

    /**
     * TODO: explain
     *
     * NOT FOR LJs!!!
     *
     */
    private Optional<PushDownBooleanExpressionProposal> makeProposalForFilterOrCommutativeJoin(
            IntermediateQuery currentQuery, JoinOrFilterNode currentNode) {

        Optional<ImmutableBooleanExpression> optionalNestedExpression = currentNode.getOptionalFilterCondition();
        if (!optionalNestedExpression.isPresent()) {
            return Optional.absent();
        }

        ImmutableSet<ImmutableBooleanExpression> booleanExpressions = optionalNestedExpression.get().flatten();

        ImmutableList<DelimiterTargetPair> potentialTargetNodes = findCandidateTargetNodes(currentQuery, currentNode);

        ImmutableMultimap.Builder<QueryNode, ImmutableBooleanExpression> transferMapBuilder = ImmutableMultimap.builder();
        ImmutableList.Builder<ImmutableBooleanExpression> notTransferedExpressionBuilder = ImmutableList.builder();

        for (ImmutableBooleanExpression expression : booleanExpressions) {
            ImmutableList<QueryNode> nodesForTransfer = selectNodesForTransfer(expression, potentialTargetNodes);
            for (QueryNode targetNode : nodesForTransfer) {
                transferMapBuilder.put(targetNode, expression);
            }
            if (nodesForTransfer.isEmpty()) {
                notTransferedExpressionBuilder.add(expression);
            }
        }

        return buildProposal(currentNode, transferMapBuilder.build(), notTransferedExpressionBuilder.build());
    }

    private Optional<PushDownBooleanExpressionProposal> buildProposal(
            JoinOrFilterNode focusNode, ImmutableMultimap<QueryNode, ImmutableBooleanExpression> transferMap,
            ImmutableList<ImmutableBooleanExpression> notTransferedExpressions) {
        if (transferMap.isEmpty()) {
            return Optional.absent();
        }
        else {
            PushDownBooleanExpressionProposal proposal = new PushDownBooleanExpressionProposalImpl(
                    focusNode, transferMap, notTransferedExpressions);
            return Optional.of(proposal);
        }
    }

    /**
     * TODO: explain
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
     * TODO: find a better name
     *
     * TODO: explain
     */
    private ImmutableList<DelimiterTargetPair> findCandidateTargetNodes(IntermediateQuery currentQuery,
                                                                        JoinOrFilterNode currentNode) {
        try {
            ImmutableList.Builder<DelimiterTargetPair> candidateListBuilder = ImmutableList.builder();

            for (QueryNode childNode : currentQuery.getChildren(currentNode)) {
                candidateListBuilder.addAll(findCandidatesInSubTree(currentQuery, childNode,
                        Optional.<SubTreeDelimiterNode>absent()));
            }
            return candidateListBuilder.build();
        } catch (NotSupportedCaseException e) {
            return ImmutableList.of();
        }
    }

    /**
     * TODO: explain and clean
     */
    private ImmutableList<DelimiterTargetPair> findCandidatesInSubTree(IntermediateQuery currentQuery, QueryNode node,
                                                                       final Optional<SubTreeDelimiterNode> optionalClosestDelimiterNode)
            throws NotSupportedCaseException {

        /**
         * First leaf case: ...
         */
        if (node instanceof DataNode) {
            if (optionalClosestDelimiterNode.isPresent()) {
                return ImmutableList.of(new DelimiterTargetPair(optionalClosestDelimiterNode.get(), node));
            }
            else {
                throw new NotSupportedCaseException();
            }
        }

        /**
         * Second leaf case: ...
         */
        if ((node instanceof JoinOrFilterNode) && optionalClosestDelimiterNode.isPresent()) {
            return ImmutableList.of(new DelimiterTargetPair(optionalClosestDelimiterNode.get(), node));
        }

        /**
         * Otherwise, looks at the children
         */
        Optional<SubTreeDelimiterNode> newOptionalClosestDelimiterNode;

        /**
         *     Updates the closest SubTreeDelimiterNode
         */
        if (node instanceof SubTreeDelimiterNode) {
            SubTreeDelimiterNode delimiterNode = (SubTreeDelimiterNode) node;
            newOptionalClosestDelimiterNode = Optional.of(delimiterNode);

            /**
             * Special case: some delimiter nodes are ALSO BE COMMUTATIVE JOINS
             * (in some extensions of Ontop)
             */
            if (node instanceof CommutativeJoinNode) {
                return ImmutableList.of(new DelimiterTargetPair(delimiterNode, delimiterNode));
            }
        }
        else {
            newOptionalClosestDelimiterNode = optionalClosestDelimiterNode;
        }

        /**
         *    Gathers the pairs returned by the children
         */
        ImmutableList.Builder<DelimiterTargetPair> candidateListBuilder = ImmutableList.builder();
        for (QueryNode child : currentQuery.getChildren(node)) {
            // Recursive call
            candidateListBuilder.addAll(findCandidatesInSubTree(currentQuery, child, newOptionalClosestDelimiterNode));
        }
        return candidateListBuilder.build();
    }

}
