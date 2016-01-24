package org.semanticweb.ontop.owlrefplatform.core.optimization;

import org.semanticweb.ontop.pivotalrepr.InnerJoinNode;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Top-down exploration.
 */
public class BasicJoinOptimizer extends NodeCentricTopDownOptimizer<InnerJoinOptimizationProposal> {

    public BasicJoinOptimizer() {
        super(true);
    }

    @Override
    protected Optional<InnerJoinOptimizationProposal> evaluateNode(QueryNode node) {
        return Optional.of(node)
                .filter(n -> n instanceof InnerJoinNode)
                .map(n -> (InnerJoinNode) n)
                .map(InnerJoinOptimizationProposalImpl::new);
    }

}
