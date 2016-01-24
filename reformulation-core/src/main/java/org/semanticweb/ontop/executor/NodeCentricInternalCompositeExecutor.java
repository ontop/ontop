package org.semanticweb.ontop.executor;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;

/**
 * TODO: explain
 */
public abstract class NodeCentricInternalCompositeExecutor<N extends QueryNode, P extends NodeCentricOptimizationProposal<N>>
        extends InternalCompositeExecutor<P, NodeCentricOptimizationResults<N>> implements NodeCentricInternalExecutor<N, P> {

    @Override
    protected Optional<P> createNewProposal(NodeCentricOptimizationResults<N> results) {
        Optional<N> optionalNewNode = results.getOptionalNewNode()
                .map(Optional::of)
                .orElse(Optional.empty());

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
