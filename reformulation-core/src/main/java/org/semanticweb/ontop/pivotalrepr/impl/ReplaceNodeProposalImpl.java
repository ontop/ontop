package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.InvalidLocalOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.ReplaceNodeProposal;


public class ReplaceNodeProposalImpl extends LocalOptimizationProposalImpl implements ReplaceNodeProposal {
    private final QueryNode newNode;

    public ReplaceNodeProposalImpl(IntermediateQuery intermediateQuery, QueryNode formerNode, QueryNode newNode) {
        super(formerNode, intermediateQuery);
        this.newNode = newNode;
    }

    @Override
    public QueryNode apply() throws InvalidLocalOptimizationProposalException {
        return getTargetQuery().applyReplaceNodeProposal(this);
    }

    @Override
    public QueryNode getReplacingNode() {
        return newNode;
    }
}