package it.unibz.inf.ontop.executor;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;

/**
 * TODO: explain
 */
public abstract class NodeCentricInternalCompositeExecutor<N extends QueryNode, P extends NodeCentricOptimizationProposal<N>>
        extends InternalCompositeExecutor<P, NodeCentricOptimizationResults<N>> implements NodeCentricInternalExecutor<N, P> {

    @Override
    protected Optional<P> createNewProposal(NodeCentricOptimizationResults<N> results) {
        Optional<N> optionalNewNode = results.getOptionalNewNode()
                .map(Optional::of)
                .orElseGet(Optional::empty);

        if (optionalNewNode.isPresent()) {
            return createNewProposalFromFocusNode(optionalNewNode.get());
        }
        else {
            return Optional.empty();
        }

    }

    protected abstract Optional<P> createNewProposalFromFocusNode(N focusNode);

    @Override
    protected abstract ImmutableList<NodeCentricInternalExecutor<N, P>> createExecutors();
}
