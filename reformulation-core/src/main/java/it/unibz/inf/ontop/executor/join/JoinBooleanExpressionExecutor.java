package it.unibz.inf.ontop.executor.join;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalExecutor;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.impl.EmptyNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.InnerJoinNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveEmptyNodeProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.RemoveEmptyNodeProposalImpl;

import java.util.Optional;

import static it.unibz.inf.ontop.executor.join.JoinExtractionUtils.*;

/**
* TODO: explain
*/
public class JoinBooleanExpressionExecutor implements InnerJoinExecutor {

    /**
     * Standard method (InternalProposalExecutor)
     */
    @Override
    public NodeCentricOptimizationResults<InnerJoinNode> apply(InnerJoinOptimizationProposal proposal, IntermediateQuery query,
                                              QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        InnerJoinNode originalTopJoinNode = proposal.getFocusNode();

        ImmutableList<JoinOrFilterNode> filterOrJoinNodes = extractFilterAndInnerJoinNodes(originalTopJoinNode, query);

        QueryNode parentNode = query.getParent(originalTopJoinNode).get();

        Optional<ImmutableExpression> optionalAggregatedFilterCondition;
        try {
            optionalAggregatedFilterCondition = extractFoldAndOptimizeBooleanExpressions(filterOrJoinNodes,
                    query.getMetadata());
        }
        /**
         * The filter condition cannot be satisfied --> the join node and its sub-tree is thus removed from the tree.
         * Returns no join node.
         */
        catch (UnsatisfiableExpressionException e) {

            EmptyNode replacingEmptyNode = new EmptyNodeImpl(query.getVariables(originalTopJoinNode));
            treeComponent.replaceSubTree(originalTopJoinNode, replacingEmptyNode);

            RemoveEmptyNodeProposal cleaningProposal = new RemoveEmptyNodeProposalImpl(replacingEmptyNode, false);

            NodeCentricOptimizationResults<EmptyNode> cleaningResults = query.applyProposal(cleaningProposal, true);

            // Converts it into a NodeCentricOptimizationResults<InnerJoinNode>
            return new NodeCentricOptimizationResultsImpl<>(cleaningResults.getResultingQuery(),
                    cleaningResults.getOptionalNextSibling(), cleaningResults.getOptionalClosestAncestor());
        }

        /**
         * If something has changed
         */
        if ((filterOrJoinNodes.size() > 1)
                || (!optionalAggregatedFilterCondition.equals(originalTopJoinNode.getOptionalFilterCondition()))) {
            /**
             * Optimized join node
             */
            InnerJoinNode newJoinNode = new InnerJoinNodeImpl(optionalAggregatedFilterCondition);

            Optional<ArgumentPosition> optionalPosition = treeComponent.getOptionalPosition(parentNode, originalTopJoinNode);
            treeComponent.replaceNodesByOneNode(ImmutableList.<QueryNode>copyOf(filterOrJoinNodes), newJoinNode, parentNode,
                    optionalPosition);

            return new NodeCentricOptimizationResultsImpl<>(query, newJoinNode);
        }
        else {
            return new NodeCentricOptimizationResultsImpl<>(query, originalTopJoinNode);
        }
    }



}
