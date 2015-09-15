package org.semanticweb.ontop.executor.join;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.executor.InternalProposalExecutor;
import org.semanticweb.ontop.executor.NodeCentricInternalCompositeExecutor;
import org.semanticweb.ontop.pivotalrepr.InnerJoinNode;
import org.semanticweb.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;

/**
 * TODO: explain
 */
public class JoinInternalCompositeExecutor extends NodeCentricInternalCompositeExecutor<InnerJoinOptimizationProposal, InnerJoinNode> {

    @Override
    protected Optional<InnerJoinOptimizationProposal> createNewProposalFromFocusNode(InnerJoinNode focusNode) {
        InnerJoinOptimizationProposal proposal = new InnerJoinOptimizationProposalImpl(focusNode);
        return Optional.of(proposal);
    }

    @Override
    protected ImmutableList<InternalProposalExecutor<InnerJoinOptimizationProposal>> createExecutors() {
        ImmutableList.Builder<InternalProposalExecutor<InnerJoinOptimizationProposal>> executorBuilder = ImmutableList.builder();

        executorBuilder.add(new JoinBooleanExpressionExecutor());
        // TODO: add redundant join elimination

        return executorBuilder.build();
    }
}
