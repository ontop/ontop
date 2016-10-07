package it.unibz.inf.ontop.executor.join;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalCompositeExecutor;
import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalExecutor;
import it.unibz.inf.ontop.pivotalrepr.InnerJoinNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;

import java.util.Optional;

/**
 * TODO: explain
 */
public class JoinInternalCompositeExecutor
        extends SimpleNodeCentricInternalCompositeExecutor<InnerJoinNode, InnerJoinOptimizationProposal>
        implements InnerJoinExecutor {

    @Override
    protected Optional<InnerJoinOptimizationProposal> createNewProposalFromFocusNode(InnerJoinNode focusNode) {
        InnerJoinOptimizationProposal proposal = new InnerJoinOptimizationProposalImpl(focusNode);
        return Optional.of(proposal);
    }

    @Override
    protected ImmutableList<SimpleNodeCentricInternalExecutor<InnerJoinNode, InnerJoinOptimizationProposal>> createExecutors() {

        ImmutableList.Builder<SimpleNodeCentricInternalExecutor<InnerJoinNode, InnerJoinOptimizationProposal>> executorBuilder = ImmutableList.builder();

        executorBuilder.add(new JoinBooleanExpressionExecutor());
        executorBuilder.add(new RedundantSelfJoinExecutor());

        return executorBuilder.build();
    }
}
