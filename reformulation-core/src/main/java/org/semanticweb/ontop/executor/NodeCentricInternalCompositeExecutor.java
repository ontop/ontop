package org.semanticweb.ontop.executor;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.executor.join.JoinBooleanExpressionExecutor;
import org.semanticweb.ontop.pivotalrepr.InnerJoinNode;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;

/**
 * TODO: explain
 */
public abstract class NodeCentricInternalCompositeExecutor<N extends QueryNode, P extends NodeCentricOptimizationProposal<N>>
        extends InternalCompositeExecutor<P, NodeCentricOptimizationResults<N>> implements NodeCentricInternalExecutor<N, P> {

    @Override
    protected Optional<P> createNewProposal(NodeCentricOptimizationResults<N> results) {
        Optional<N> optionalNewNode = results.getOptionalNewNode();
        if (optionalNewNode.isPresent()) {
            return createNewProposalFromFocusNode(optionalNewNode.get());
        }
        else {
            return Optional.absent();
        }

    }

    protected abstract Optional<P> createNewProposalFromFocusNode(N focusNode);

    @Override
    protected abstract ImmutableList<NodeCentricInternalExecutor<N, P>> createExecutors();
}
