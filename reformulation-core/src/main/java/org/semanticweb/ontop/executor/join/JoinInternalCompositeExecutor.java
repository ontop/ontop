package org.semanticweb.ontop.executor.join;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.executor.NodeCentricInternalCompositeExecutor;
import org.semanticweb.ontop.executor.NodeCentricInternalExecutor;
import org.semanticweb.ontop.pivotalrepr.InnerJoinNode;
import org.semanticweb.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;

/**
 * TODO: explain
 */
public class JoinInternalCompositeExecutor extends NodeCentricInternalCompositeExecutor<InnerJoinNode, InnerJoinOptimizationProposal> {

    @Override
    protected Optional<InnerJoinOptimizationProposal> createNewProposalFromFocusNode(InnerJoinNode focusNode) {
        InnerJoinOptimizationProposal proposal = new InnerJoinOptimizationProposalImpl(focusNode);
        return Optional.of(proposal);
    }

    @Override
    protected ImmutableList<NodeCentricInternalExecutor<InnerJoinNode, InnerJoinOptimizationProposal>> createExecutors() {
        ImmutableList.Builder<NodeCentricInternalExecutor<InnerJoinNode, InnerJoinOptimizationProposal>> executorBuilder = ImmutableList.builder();

        executorBuilder.add(new JoinBooleanExpressionExecutor());
        // executorBuilder.add(new RedundantSelfJoinExecutor());

        return executorBuilder.build();
    }
}
