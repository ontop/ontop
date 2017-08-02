package it.unibz.inf.ontop.iq.executor;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationProposal;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;

import java.util.Optional;

/**
 * TODO: explain
 */
public abstract class NodeCentricCompositeExecutor<
        N extends QueryNode,
        R extends NodeCentricOptimizationResults<N>,
        P extends NodeCentricOptimizationProposal<N, R>>
        extends InternalCompositeExecutor<P, R>
        implements NodeCentricExecutor<N, R, P> {

    @Override
    protected Optional<P> createNewProposal(R results) {
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
    protected abstract ImmutableList<? extends NodeCentricExecutor<N, R, P>> getExecutors();
}
