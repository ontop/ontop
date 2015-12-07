package org.semanticweb.ontop.pivotalrepr;

import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;
import org.semanticweb.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

public interface UnionLiftProposal extends QueryOptimizationProposal<ProposalResults> {

    UnionNode getUnionNode();

}
