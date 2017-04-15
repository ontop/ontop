package it.unibz.inf.ontop.owlrefplatform.core.optimization;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.pivotalrepr.InnerJoinNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;

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
