package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public interface IntermediateQuery {


    public ImmutableList<QueryNode> getNodesInBottomUpOrder();

    public ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node);

    /**
     * TODO: replace OptimizationProposal argument by a more specific one.
     *
     * Applies the local optimization proposal to the DAG.
     *
     * Returns the QueryNode at the same position, which might be new.
     */
    public QueryNode applyOptimizationProposal(LocalOptimizationProposal proposal);

}
