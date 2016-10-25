package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import it.unibz.inf.ontop.pivotalrepr.LeftJoinNode;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.LeftJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.LeftJoinOptimizationProposalImpl;

import java.util.Optional;

/**
 *
 */
public class BasicLeftJoinOptimizer extends NodeCentricDepthFirstOptimizer<LeftJoinOptimizationProposal> {

    public BasicLeftJoinOptimizer() {
        super(true);
    }

    @Override
    protected Optional<LeftJoinOptimizationProposal> evaluateNode(QueryNode node) {
        return Optional.of(node)
                .filter(n -> n instanceof LeftJoinNode)
                .map(n -> (LeftJoinNode) n)
                .map(LeftJoinOptimizationProposalImpl::new);
    }

}
