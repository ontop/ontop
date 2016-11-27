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
 * If needed, a FilterNode is created for this purpose.
 * <p>
 * Note that is may be desirable to make implicit boolean expressions (variable equalities) explicit beforehand,
 * using the dedicated optimizer.
 * <p>
 * The rules for propagating up are the following.
 * An expression e is always propagated up, until we reach:
 * - a left join node j from its right subtree: j becomes the new recipient,
 * and e is not propagated further.
 * - a union node u (resp. the root construction node r): e is not propagated further,
 * and for each child n of u (resp. the unique child n of r),
 * if n is a filter or inner join,
 * then n becomes the recipient for e.
 * Otherwise a fresh filter node is inserted between u (resp. r) and n to support e.
 * <p>
 * <p>
 * Note that some projections may need to be extended.
 * More exactly, each construction node on the path from the provider to the recipient node will see its set of projected
 * variables extended with all variables appearing in e.
 * <p>
 *
 * TODO: can be improved by propagating up (sub) boolean expressions which are present in all branches of a union node,
 */

public class PushUpBooleanExpressionOptimizerImpl implements PushUpBooleanExpressionOptimizer {

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        return optimizeSubtree(query.getRootConstructionNode(), query);
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
        QueryNode currentParentNode = currentChildNode;
        Optional<JoinOrFilterNode> recipient;

        do {
            currentChildNode = currentParentNode;
            currentParentNode = query.getParent(currentParentNode)
                    .orElseThrow(() -> new InvalidIntermediateQueryException("This node must have a parent node"));
            if (currentParentNode instanceof ConstructionNode && currentParentNode != query.getRootConstructionNode()) {
                inbetweenProjectorsBuilder.add((ExplicitVariableProjectionNode) currentParentNode);
            }
            if (currentParentNode instanceof LeftJoinNode) {
                recipient = Optional.of((JoinOrFilterNode) currentParentNode);
                if (query.getOptionalPosition(focusNode)
                        .orElseThrow(() -> new IllegalStateException("The child of a LeftJoin node must have a position"))
                        == RIGHT) {
                    return Optional.of(
                            new PushUpBooleanExpressionProposalImpl(focusNode, currentParentNode, recipient, inbetweenProjectorsBuilder.build()));
                }
            }
        }
        while (!(currentParentNode instanceof UnionNode) && (currentParentNode != query.getRootConstructionNode()));

        if (currentChildNode == focusNode) {
            return Optional.empty();
        }
        recipient = currentChildNode instanceof CommutativeJoinOrFilterNode ?
                Optional.of((CommutativeJoinOrFilterNode) currentChildNode) :
                Optional.empty();

        return Optional.of(new PushUpBooleanExpressionProposalImpl(focusNode, currentParentNode, recipient, inbetweenProjectorsBuilder.build()));
    }
}
