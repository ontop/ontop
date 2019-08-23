package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.InnerJoinOptimizationProposal;
import it.unibz.inf.ontop.iq.proposal.impl.InnerJoinOptimizationProposalImpl;
import it.unibz.inf.ontop.iq.optimizer.InnerJoinOptimizer;

import java.util.Optional;

/**
 * TODO: explain
 *
 * Top-down exploration.
 */
@Singleton
public class BasicJoinOptimizer extends NodeCentricDepthFirstOptimizer<InnerJoinOptimizationProposal>
        implements InnerJoinOptimizer {

    @Inject
    private BasicJoinOptimizer() {
        super(true);
    }


    private Optional<InnerJoinOptimizationProposal> evaluateNode(QueryNode node) {
        return Optional.of(node)
                .filter(n -> n instanceof InnerJoinNode)
                .map(n -> (InnerJoinNode) n)
                .map(InnerJoinOptimizationProposalImpl::new);
    }

    @Override
    protected Optional<InnerJoinOptimizationProposal> evaluateNode(QueryNode node, IntermediateQuery query) {
        return evaluateNode(node);
    }

}
