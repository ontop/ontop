package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.iq.optimizer.IntermediateQueryOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.QueryNodeNavigationTools.NextNodeAndQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.SimpleNodeCentricOptimizationProposal;

import java.util.Optional;

import static it.unibz.inf.ontop.iq.optimizer.impl.QueryNodeNavigationTools.*;

/**
 * Optimizer that evaluates the QueryNode-s one by one in a Depth-First order.
 *
 * When evaluating one QueryNode, it can make one proposal.
 */
public abstract class NodeCentricDepthFirstOptimizer<P extends SimpleNodeCentricOptimizationProposal<? extends QueryNode>>
        implements IntermediateQueryOptimizer {

    private final boolean canEmptyQuery;

    protected NodeCentricDepthFirstOptimizer(boolean canEmptyQuery) {
        this.canEmptyQuery = canEmptyQuery;
    }

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        try {
            return optimizeQuery(query);
        } catch (EmptyQueryException e) {
            if (canEmptyQuery) {
                throw e;
            }
            else {
                throw new IllegalStateException("Inconsistency: " + this + " should not empty the query");
            }
        }
    }

    protected IntermediateQuery optimizeQuery(IntermediateQuery initialQuery) throws EmptyQueryException {

        // Non-final
        Optional<QueryNode> optionalNextNode = Optional.of(initialQuery.getRootNode());

        // Non-final
        IntermediateQuery currentQuery = initialQuery;

        while (optionalNextNode.isPresent()) {
            QueryNode currentNode = optionalNextNode.get();

            Optional<P> optionalProposal = evaluateNode(currentNode,currentQuery);

            if (optionalProposal.isPresent()) {
                NodeCentricOptimizationResults<? extends QueryNode> optimizationResults = currentQuery.applyProposal(
                        optionalProposal.get());

                NextNodeAndQuery nextNodeAndQuery = getNextNodeAndQuery(currentQuery, optimizationResults);
                currentQuery = nextNodeAndQuery.getNextQuery();
                optionalNextNode = nextNodeAndQuery.getOptionalNextNode();
            }
            else {
                optionalNextNode = getDepthFirstNextNode(currentQuery, currentNode);
            }
        }
        return currentQuery;
    }

    protected abstract Optional<P> evaluateNode(QueryNode node, IntermediateQuery query);
}
