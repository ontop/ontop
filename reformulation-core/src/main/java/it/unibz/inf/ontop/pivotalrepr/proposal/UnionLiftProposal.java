package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.UnionNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

public interface UnionLiftProposal extends SimpleNodeCentricOptimizationProposal<UnionNode> {

    /**
     * The Union has to be lift just above this target node
     */
    QueryNode getTargetNode();
}
