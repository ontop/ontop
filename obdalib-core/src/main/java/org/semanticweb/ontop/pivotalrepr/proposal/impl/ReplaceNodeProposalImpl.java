package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidLocalOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.ReplaceNodeProposal;


@Deprecated
public class ReplaceNodeProposalImpl implements ReplaceNodeProposal {
    private final QueryNode newNode;
    private final QueryNode formerNode;
    private final IntermediateQuery query;

    public ReplaceNodeProposalImpl(IntermediateQuery intermediateQuery, QueryNode formerNode, QueryNode newNode) {
        this.query = intermediateQuery;
        this.formerNode = formerNode;
        this.newNode = newNode;
    }

    @Override
    public Optional<QueryNode> apply() throws InvalidLocalOptimizationProposalException {
        return Optional.of(query.applyReplaceNodeProposal(this));
    }

    @Override
    public QueryNode getNodeToReplace() {
        return formerNode;
    }

    @Override
    public QueryNode getReplacingNode() {
        return newNode;
    }
}