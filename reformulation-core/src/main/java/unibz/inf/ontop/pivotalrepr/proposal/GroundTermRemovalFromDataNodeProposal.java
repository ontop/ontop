package unibz.inf.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import unibz.inf.ontop.pivotalrepr.DataNode;

/**
 * TODO: explain
 */
public interface GroundTermRemovalFromDataNodeProposal extends QueryOptimizationProposal<ProposalResults> {

    /**
     * TODO: find a better name
     */
    ImmutableList<DataNode> getDataNodesToSimplify();
}
