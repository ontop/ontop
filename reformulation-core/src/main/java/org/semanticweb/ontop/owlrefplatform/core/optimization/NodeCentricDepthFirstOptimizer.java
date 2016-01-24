package org.semanticweb.ontop.owlrefplatform.core.optimization;

import org.semanticweb.ontop.pivotalrepr.EmptyQueryException;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;

import java.util.Optional;

import static org.semanticweb.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.*;
import static org.semanticweb.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getNextNodeAndQuery;

/**
 * Optimizer that evaluates the QueryNode-s one by one in a Depth-First order.
 *
 * When evaluating one QueryNode, it can make one proposal.
 */
public abstract class NodeCentricDepthFirstOptimizer<P extends NodeCentricOptimizationProposal<? extends QueryNode>>
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

    private IntermediateQuery optimizeQuery(IntermediateQuery initialQuery) throws EmptyQueryException {

        // Non-final
        Optional<QueryNode> optionalNextNode = Optional.of((QueryNode)initialQuery.getRootConstructionNode());

        // Non-final
        IntermediateQuery currentQuery = initialQuery;

        while (optionalNextNode.isPresent()) {
            QueryNode currentNode = optionalNextNode.get();

            Optional<P> optionalProposal = evaluateNode(currentNode);

            if (optionalProposal.isPresent()) {
                NodeCentricOptimizationResults<? extends QueryNode> optimizationResults = currentQuery.applyProposal(
                        optionalProposal.get());

                NextNodeAndQuery nextNodeAndQuery = getNextNodeAndQuery(optimizationResults);
                currentQuery = nextNodeAndQuery.getNextQuery();
                optionalNextNode = nextNodeAndQuery.getOptionalNextNode();
            }
            else {
                optionalNextNode = getDepthFirstNextNode(currentQuery, currentNode);
            }
        }
        return currentQuery;
    }

    /**
     * TODO: do we need also the query?
     */
    protected abstract Optional<P> evaluateNode(QueryNode node);

}
