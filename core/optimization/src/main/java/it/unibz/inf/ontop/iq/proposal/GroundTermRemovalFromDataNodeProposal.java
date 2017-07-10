package it.unibz.inf.ontop.iq.proposal;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.DataNode;

/**
 * TODO: explain
 */
public interface GroundTermRemovalFromDataNodeProposal extends QueryOptimizationProposal<ProposalResults> {

    /**
     * TODO: find a better name
     */
    ImmutableList<DataNode> getDataNodesToSimplify();
}
