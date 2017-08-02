package it.unibz.inf.ontop.iq.proposal;

import it.unibz.inf.ontop.iq.node.QueryNode;

/**
 * For proposals that do not return results of a sub-interface of NodeCentricOptimizationResults
 */
public interface SimpleNodeCentricOptimizationProposal<N extends QueryNode>
        extends NodeCentricOptimizationProposal<N, NodeCentricOptimizationResults<N>> {

}
