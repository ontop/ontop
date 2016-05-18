package it.unibz.inf.ontop.executor.unsatisfiable;

import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveUnsatisfiableNodesProposal;

/**
 * TODO: explain
 */
public class RemoveUnsatisfiableNodesExecutor implements InternalProposalExecutor<RemoveUnsatisfiableNodesProposal, ProposalResults> {

    @Override
    public ProposalResults apply(RemoveUnsatisfiableNodesProposal proposal, IntermediateQuery query,
                                 QueryTreeComponent treeComponent)
            throws EmptyQueryException {
        throw new RuntimeException("TODO: implement the RemoveUnsatisfiableNodesExecutor");
    }
}
