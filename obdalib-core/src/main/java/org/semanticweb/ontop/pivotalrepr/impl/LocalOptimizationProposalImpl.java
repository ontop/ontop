package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.LocalOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

/**
 * Abstract class
 */
public abstract class LocalOptimizationProposalImpl implements LocalOptimizationProposal {

    private final QueryNode queryNode;
    private final IntermediateQuery targetQuery;

    protected LocalOptimizationProposalImpl(QueryNode queryNode, IntermediateQuery targetQuery) {
        this.queryNode = queryNode;
        this.targetQuery = targetQuery;
    }

    @Override
    public QueryNode getQueryNode() {
        return queryNode;
    }

    @Override
    public IntermediateQuery getTargetQuery() {
        return targetQuery;
    }
}
