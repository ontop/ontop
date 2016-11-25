package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushUpBooleanExpressionProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.PushUpBooleanExpressionProposalImpl;
import it.unibz.inf.ontop.pivotalrepr.validation.InvalidIntermediateQueryException;

import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;

/**
 * Tries to push (explicit) boolean expressions as high as possible in the algebraic tree.
 * The optimized query has a FilterNode or InnerJoinNode as the child of the root ConstructionNode,
 * which supports all pushed up boolean expressions.
 * If needed, a FilterNode is created for this purpose.
 * <p>
 * Note that is may be desirable to make implicit boolean expressions (variable equalities) explicit beforehand,
 * using the dedicated optimizer.
 * <p>
 * The rules for propagating up are simple.
 * The recipient of an expression e being propagated up can only be:
 * - a left join node, iff e is propagated up from its right child subtree,
 * and in this case, e is not propagated further.
 * - the (FilterNode or InnerJoinNode) child of the root ConstructionNode
 * <p>
 * Note that some projections (ConstructionNode and UnionNode) may be extended during the process.
 * More exactly, each projecting node on the path from the provider to the recipient node will see its set of projected
 * variables extended with all variables appearing in e.
 */

public class PushUpBooleanExpressionOptimizerImpl implements PushUpBooleanExpressionOptimizer {

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        return optimizeSubtree(query.getRootConstructionNode(),query);
    }

    private IntermediateQuery optimizeSubtree(QueryNode focusNode, IntermediateQuery query) throws EmptyQueryException {
        Optional<QueryNode> optionalNextNode;
        Optional<PushUpBooleanExpressionProposal> optionalProposal = Optional.empty();

        if (focusNode instanceof CommutativeJoinOrFilterNode) {
            optionalProposal = makeProposal((CommutativeJoinOrFilterNode) focusNode, query);
        }
        if (optionalProposal.isPresent()) {
            NodeCentricOptimizationResults<CommutativeJoinOrFilterNode> optimizationResults = query.applyProposal(optionalProposal.get());
            QueryNodeNavigationTools.NextNodeAndQuery nextNodeAndQuery = QueryNodeNavigationTools.getNextNodeAndQuery(optimizationResults);
            query = nextNodeAndQuery.getNextQuery();
            optionalNextNode = nextNodeAndQuery.getOptionalNextNode();
        } else {
            optionalNextNode = QueryNodeNavigationTools.getDepthFirstNextNode(query, focusNode);
        }
        return (optionalNextNode.isPresent()) ?
                optimizeSubtree(optionalNextNode.get(), query) :
                query;
    }

    private Optional<PushUpBooleanExpressionProposal> makeProposal(CommutativeJoinOrFilterNode focusNode, IntermediateQuery query) {

        if (!focusNode.getOptionalFilterCondition().isPresent()) {
            return Optional.empty();
        }
        ImmutableList.Builder<ExplicitVariableProjectionNode> inbetweenProjectorsBuilder = ImmutableList.builder();
        QueryNode currentChildNode = focusNode;
        QueryNode currentParentNode;
        Optional<JoinOrFilterNode> recipient;

        do {
            currentParentNode = query.getParent(currentChildNode)
                    .orElseThrow(() -> new InvalidIntermediateQueryException("This node must have a parent node"));

            if (currentParentNode instanceof ExplicitVariableProjectionNode) {
                inbetweenProjectorsBuilder.add((ExplicitVariableProjectionNode) currentParentNode);
            }
            if (currentParentNode instanceof LeftJoinNode) {
                recipient = Optional.of((JoinOrFilterNode) currentParentNode);
                if (query.getOptionalPosition(focusNode).orElseThrow(() -> new IllegalStateException("The child of a LeftJoin node must have a position")) == RIGHT) {
                    return Optional.of(new PushUpBooleanExpressionProposalImpl(focusNode, recipient, inbetweenProjectorsBuilder.build()));
                }
            }
        }
        while (currentParentNode != query.getRootConstructionNode());
        /**
         * If this instruction is reached, the current parent node must be the root ConstructionNode
         */
        QueryNode rootChild = query.getFirstChild(currentParentNode).orElseThrow(() -> new InvalidIntermediateQueryException("this query ahs a CommutativeJoinOrFilterNode," +
                "so it has at least two nodes"));

        if (rootChild == focusNode) {
            return Optional.empty();
        }
        recipient = rootChild instanceof CommutativeJoinOrFilterNode ?
                Optional.of((CommutativeJoinNode) rootChild) :
                Optional.empty();

        return Optional.of(new PushUpBooleanExpressionProposalImpl(focusNode, recipient, inbetweenProjectorsBuilder.build()));
    }
}
