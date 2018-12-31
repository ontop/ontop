package it.unibz.inf.ontop.iq.executor.join;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.EmptyNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.JoinOrFilterNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.proposal.InnerJoinOptimizationProposal;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.Optional;

import static it.unibz.inf.ontop.iq.executor.join.JoinExtractionUtils.*;

/**
* TODO: explain
*/
@Singleton
public class JoinBooleanExpressionExecutor implements InnerJoinExecutor {

    private final IntermediateQueryFactory iqFactory;
    private final JoinExtractionUtils joinExtractionUtils;

    @Inject
    private JoinBooleanExpressionExecutor(IntermediateQueryFactory iqFactory, JoinExtractionUtils joinExtractionUtils) {
        this.iqFactory = iqFactory;
        this.joinExtractionUtils = joinExtractionUtils;
    }

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
            optionalAggregatedFilterCondition = joinExtractionUtils.extractFoldAndOptimizeBooleanExpressions(filterOrJoinNodes);
        }
        /*
         * The filter condition cannot be satisfied --> the join node and its sub-tree is thus removed from the tree.
         * Returns no join node.
         */
        catch (UnsatisfiableExpressionException e) {

            EmptyNode replacingEmptyNode = iqFactory.createEmptyNode(query.getVariables(originalTopJoinNode));
            treeComponent.replaceSubTree(originalTopJoinNode, replacingEmptyNode);

            // Converts it into a NodeCentricOptimizationResults<InnerJoinNode>
            return new NodeCentricOptimizationResultsImpl<>(query, Optional.of(replacingEmptyNode));
        }

        /*
         * If something has changed
         */
        if ((filterOrJoinNodes.size() > 1)
                || (!optionalAggregatedFilterCondition.equals(originalTopJoinNode.getOptionalFilterCondition()))) {
            /*
             * Optimized join node
             */
            InnerJoinNode newJoinNode = iqFactory.createInnerJoinNode(optionalAggregatedFilterCondition);

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
