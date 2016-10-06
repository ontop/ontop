package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.TrueNodeRemovalProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.TrueNodeRemovalProposalImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;


/**
 * Removes and or lifts TrueNodes whenever possible.
 * For each TrueNode n in the query,
 * let p be its parent node.
 *
 * If p is neither a JoinNode, a ConstructionNode or a TrueNode,
 * then no action is taken.
 *
 * If p is a left join node,
 * and n is its left child,
 * then no action is taken either.
 *
 * If p is a ConstructionNode,
 * then n is removed.
 *
 * If p is a commutative join node,
 * or if p is a left join node and n is not its left child,
 * then n is removed,
 * and:
 * - if p has no other child (this is only possible in the case of a commutative join),
 * then p becomes a TrueNode.
 * - if p has exactly one remaining child n2,
 * then p is replaced by n2
 * - if p has more than one remaining children,
 * nothing happens
 *
 *
 * Several iterations over the whole tree may need to be applied,
 * until no more TrueNode can be removed.
 * The process terminates if no TrueNode has been removed during the latest tree traversal.
 */

/**
 * TODO: create an index of TrueNodes during the first traversal of the query, to access them directly if further iterations are needed
 */

public class TrueNodesOptimizer extends NodeCentricDepthFirstOptimizer<TrueNodeRemovalProposal> {

    private final Logger log = LoggerFactory.getLogger(TrueNodesOptimizer.class);

    private  Boolean additionalIterationNeeded = true;

    TrueNodesOptimizer() {
        this(false);
    }

    TrueNodesOptimizer(boolean canEmptyQuery) {
        super(canEmptyQuery);
    }

    protected boolean isRemovableTrueNode(TrueNode node, IntermediateQuery currentQuery) {
        Optional<QueryNode> parentNode = currentQuery.getParent(node);
        if (parentNode.get() instanceof InnerJoinNode ||
                parentNode.get() instanceof ConstructionNode ||
                parentNode.get() instanceof TrueNode){
            this.additionalIterationNeeded =true;
            return true;
        }
        if (parentNode.get() instanceof LeftJoinNode){
            this.additionalIterationNeeded =true;
            return currentQuery.getOptionalPosition(node).equals(NonCommutativeOperatorNode.ArgumentPosition.RIGHT);
        }
        return false;
    }



    @Override
    protected Optional<TrueNodeRemovalProposal> evaluateNode(QueryNode currentNode, IntermediateQuery currentQuery) {
        return Optional.of(currentNode).
                filter(n -> n instanceof TrueNode).
                map(n -> (TrueNode) n).
                filter(n -> isRemovableTrueNode(n, currentQuery)).
                map(TrueNodeRemovalProposalImpl::new);
    }

    @Override
    protected IntermediateQuery optimizeQuery(IntermediateQuery intermediateQuery) throws EmptyQueryException {
        while (additionalIterationNeeded){
            additionalIterationNeeded=false;
            log.debug("\n"+intermediateQuery.toString());
            intermediateQuery = super.optimizeQuery(intermediateQuery);
        }
        return intermediateQuery;
    }
}
