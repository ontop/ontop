package it.unibz.inf.ontop.iq.executor.pullout;

import it.unibz.inf.ontop.iq.executor.ProposalExecutor;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.iq.proposal.PullVariableOutOfSubTreeProposal;
import it.unibz.inf.ontop.iq.proposal.PullVariableOutOfSubTreeResults;

/**
 * TODO: explain
 */
public interface PullVariableOutOfSubTreeExecutor<N extends JoinLikeNode>
        extends ProposalExecutor<PullVariableOutOfSubTreeProposal<N>, PullVariableOutOfSubTreeResults<N>> {
}
