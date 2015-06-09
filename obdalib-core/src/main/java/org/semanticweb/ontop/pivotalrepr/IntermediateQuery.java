package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public interface IntermediateQuery {

    ProjectionNode getRootProjectionNode();

    ImmutableList<QueryNode> getNodesInBottomUpOrder();

    ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node);

    /**
     * TODO: describe
     *
     * Returns the QueryNode at the same position, which might be new.
     */
    QueryNode applySubNodeSelectionProposal(NewSubNodeSelectionProposal proposal)
            throws InvalidLocalOptimizationProposalException;

    /**
     * TODO: describe
     *
     * Returns the QueryNode at the same position, which might be new.
     */
    QueryNode applyReplaceNodeProposal(ReplaceNodeProposal proposal)
            throws InvalidLocalOptimizationProposalException;

    /**
     * TODO: find an exception to throw
     */
    void mergeSubQuery(IntermediateQuery subQuery) throws QueryMergingException;

}
