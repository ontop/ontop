package it.unibz.inf.ontop.iq.optimizer;

import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.proposal.TrueNodeRemovalProposal;
import it.unibz.inf.ontop.iq.proposal.impl.TrueNodeRemovalProposalImpl;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.optimizer.impl.NodeCentricDepthFirstOptimizer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;

/**
 * Removes TrueNodes whenever possible.
 * <p>
 * For each TrueNode n in the query,
 * let p be its parent node.
 * <p>
 * If p is neither a (left or inner) join node or a construction node,
 * then no action is taken.
 * <p>
 * If p is a left join node,
 * and n is its left child,
 * then no action is taken either.
 * <p>
 * If p is an inner join node,
 * or if p is a left join node and n is not its left child,
 * then n is removed,
 * and:
 * - if p has exactly one remaining child n2,
 * then p is replaced by n2
 * - if p has more than one remaining children,
 * nothing happens
 * <p>
 * <p>
 * Several iterations over the whole tree may need to be applied,
 * until no more TrueNode can be removed.
 * The process terminates if no TrueNode has been removed during the latest tree traversal.
 */


public class TrueNodesRemovalOptimizer extends NodeCentricDepthFirstOptimizer<TrueNodeRemovalProposal> {

    public TrueNodesRemovalOptimizer() {
        super(false);
    }

    @Override
    protected Optional<TrueNodeRemovalProposal> evaluateNode(QueryNode currentNode, IntermediateQuery currentQuery) {
        return Optional.of(currentNode).
                filter(n -> n instanceof TrueNode).
                map(n -> (TrueNode) n).
                filter(n -> isRemovableTrueNode(n, currentQuery)).
                map(TrueNodeRemovalProposalImpl::new);
    }

    private boolean isRemovableTrueNode(TrueNode node, IntermediateQuery query) {
        QueryNode parentNode = query.getParent(node)
                .orElseThrow(() -> new InvalidIntermediateQueryException("a TrueNode should have a parent node"));

        return parentNode instanceof InnerJoinNode ||
                parentNode instanceof TrueNode ||
                (parentNode instanceof LeftJoinNode &&
                        query.getOptionalPosition(node)
                                .orElseThrow(() -> new IllegalStateException("Children of a LJ must have positions"))
                                == RIGHT);
    }

    /**
     * Uses the index of TrueNodes in the current query, instead of the inherited tree traversal method
     * NodeCentricDepthFirstOptimizer.optimizeQuery()
     */
    @Override
    protected IntermediateQuery optimizeQuery(IntermediateQuery intermediateQuery) throws EmptyQueryException {
        boolean iterate = true;
        while (iterate) {
            List<TrueNode> trueNodes = intermediateQuery.getTrueNodes().collect(Collectors.toList());
            iterate = false;
            for (TrueNode trueNode : trueNodes) {
                Optional<TrueNodeRemovalProposal> optionalProposal = evaluateNode(trueNode, intermediateQuery);
                if (optionalProposal.isPresent()) {
                    intermediateQuery.applyProposal(optionalProposal.get());
                    iterate = true;
                }
            }
        }
        return intermediateQuery;
    }
}
