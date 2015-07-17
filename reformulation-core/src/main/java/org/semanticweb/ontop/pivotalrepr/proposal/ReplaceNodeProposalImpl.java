package org.semanticweb.ontop.pivotalrepr.proposal;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;


public class ReplaceNodeProposalImpl extends LocalOptimizationProposalImpl implements ReplaceNodeProposal {
    private final QueryNode newNode;
    private final QueryNode formerNode;

    public ReplaceNodeProposalImpl(IntermediateQuery intermediateQuery, QueryNode formerNode, QueryNode newNode) {
        super(intermediateQuery);
        this.formerNode = formerNode;
        this.newNode = newNode;
    }

    @Override
    public Optional<QueryNode> apply() throws InvalidLocalOptimizationProposalException {
        return Optional.of(getTargetQuery().applyReplaceNodeProposal(this));
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