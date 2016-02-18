package it.unibz.inf.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.SubTreeDelimiterNode;

/**
 * TODO: explain
 *
 * TODO: make explicit the treatment that is expected to be done
 *
 */
public interface PullOutVariableProposal extends NodeCentricOptimizationProposal<SubTreeDelimiterNode> {

    /**
     * Indexes of the variables to renamed.
     *
     * Indexes inside the focus node atom.
     */
    ImmutableList<Integer> getIndexes();

}
