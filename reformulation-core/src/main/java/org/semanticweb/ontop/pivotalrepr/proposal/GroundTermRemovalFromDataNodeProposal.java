package org.semanticweb.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.DataNode;

/**
 * TODO: explain
 */
public interface GroundTermRemovalFromDataNodeProposal extends QueryOptimizationProposal<ProposalResults> {

    /**
     * TODO: find a better name
     */
    ImmutableList<DataNode> getDataNodesToSimplify();
}
