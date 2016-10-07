package it.unibz.inf.ontop.executor.pullout;

import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.pivotalrepr.JoinLikeNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.PullVariableOutOfSubTreeProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.PullVariableOutOfSubTreeResults;

/**
 * TODO: explain
 */
public interface PullVariableOutOfSubTreeExecutor<N extends JoinLikeNode>
        extends InternalProposalExecutor<PullVariableOutOfSubTreeProposal<N>, PullVariableOutOfSubTreeResults<N>> {
}
