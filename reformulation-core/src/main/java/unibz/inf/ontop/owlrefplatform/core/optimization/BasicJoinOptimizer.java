package unibz.inf.ontop.owlrefplatform.core.optimization;

import unibz.inf.ontop.pivotalrepr.InnerJoinNode;
import unibz.inf.ontop.pivotalrepr.QueryNode;
import unibz.inf.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import unibz.inf.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Top-down exploration.
 */
public class BasicJoinOptimizer extends NodeCentricDepthFirstOptimizer<InnerJoinOptimizationProposal> {

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
