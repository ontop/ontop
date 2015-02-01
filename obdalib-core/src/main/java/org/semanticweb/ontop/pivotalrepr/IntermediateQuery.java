package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public interface IntermediateQuery {


    public ImmutableList<QueryNode> getNodesInBottomUpOrder();

    public ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node);

    /**
     * TODO: describe
     *
     * Returns the QueryNode at the same position, which might be new.
     */
    public QueryNode applySubNodeSelectionProposal(NewSubNodeSelectionProposal proposal)
            throws InvalidLocalOptimizationProposalException;

    /**
     * TODO: describe
     *
     * Returns the QueryNode at the same position, which might be new.
     */
    public QueryNode applyReplaceNodeProposal(ReplaceNodeProposal proposal)
            throws InvalidLocalOptimizationProposalException;

}
