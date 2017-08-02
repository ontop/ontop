package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.LeftJoinOptimizationProposal;
import it.unibz.inf.ontop.iq.proposal.impl.LeftJoinOptimizationProposalImpl;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinOptimizer;

import java.util.Optional;

/**
 *
 */
public class BasicLeftJoinOptimizer extends NodeCentricDepthFirstOptimizer<LeftJoinOptimizationProposal>
        implements LeftJoinOptimizer {

    @Inject
    private BasicLeftJoinOptimizer() {
        super(true);
    }

    @Override
    protected Optional<LeftJoinOptimizationProposal> evaluateNode(QueryNode node, IntermediateQuery query) {
        return Optional.of(node)
                .filter(n -> n instanceof LeftJoinNode)
                .map(n -> (LeftJoinNode) n)
                .map(LeftJoinOptimizationProposalImpl::new);
    }

}
