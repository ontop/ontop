package it.unibz.inf.ontop.iq.proposal;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.DataNode;

/**
 * TODO: explain
 *
 * TODO: make explicit the treatment that is expected to be done
 *
 */
public interface PullVariableOutOfDataNodeProposal extends SimpleNodeCentricOptimizationProposal<DataNode> {

    /**
     * Indexes of the variables to renamed.
     *
     * Indexes inside the focus node atom.
     */
    ImmutableList<Integer> getIndexes();

}
