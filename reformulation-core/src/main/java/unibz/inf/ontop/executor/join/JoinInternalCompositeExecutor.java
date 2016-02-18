package unibz.inf.ontop.executor.join;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import unibz.inf.ontop.pivotalrepr.InnerJoinNode;
import unibz.inf.ontop.executor.NodeCentricInternalCompositeExecutor;
import unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import unibz.inf.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import unibz.inf.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;

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
        executorBuilder.add(new RedundantSelfJoinExecutor());

        return executorBuilder.build();
    }
}
