package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.SimpleNodeCentricOptimizationProposal;

import java.util.Optional;

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
        Optional<QueryNode> optionalNextNode = Optional.of((QueryNode)initialQuery.getRootConstructionNode());

        // Non-final
        IntermediateQuery currentQuery = initialQuery;

        while (optionalNextNode.isPresent()) {
            QueryNode currentNode = optionalNextNode.get();

            Optional<P> optionalProposal = evaluateNode(currentNode,currentQuery);

            if (optionalProposal.isPresent()) {
                NodeCentricOptimizationResults<? extends QueryNode> optimizationResults = currentQuery.applyProposal(
                        optionalProposal.get());

                QueryNodeNavigationTools.NextNodeAndQuery nextNodeAndQuery = QueryNodeNavigationTools.getNextNodeAndQuery(optimizationResults);
                currentQuery = nextNodeAndQuery.getNextQuery();
                optionalNextNode = nextNodeAndQuery.getOptionalNextNode();
            }
            else {
                optionalNextNode = QueryNodeNavigationTools.getDepthFirstNextNode(currentQuery, currentNode);
            }
        }
        return currentQuery;
    }



    /**
     * TODO: do we need also the query?
     */
    protected abstract Optional<P> evaluateNode(QueryNode node, IntermediateQuery query);

}
