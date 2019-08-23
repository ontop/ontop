package it.unibz.inf.ontop.iq.executor.leftjoin;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.executor.SimpleNodeCentricCompositeExecutor;
import it.unibz.inf.ontop.iq.executor.SimpleNodeCentricExecutor;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.proposal.LeftJoinOptimizationProposal;
import it.unibz.inf.ontop.iq.proposal.impl.LeftJoinOptimizationProposalImpl;

import java.util.Optional;

/**
 * TODO: explain
 */
@Singleton
public class LeftJoinCompositeExecutor extends SimpleNodeCentricCompositeExecutor<LeftJoinNode,
        LeftJoinOptimizationProposal> implements LeftJoinExecutor {

    private final ImmutableList<SimpleNodeCentricExecutor<LeftJoinNode, LeftJoinOptimizationProposal>> executors;

    @Inject
    private LeftJoinCompositeExecutor(LeftToInnerJoinExecutor leftToInnerJoinExecutor) {
        ImmutableList.Builder<SimpleNodeCentricExecutor<LeftJoinNode, LeftJoinOptimizationProposal>> executorBuilder = ImmutableList.builder();

        //executorBuilder.add(new LeftJoinBooleanExpressionExecutor());
        executorBuilder.add(leftToInnerJoinExecutor);

        executors = executorBuilder.build();
    }

    @Override
    protected Optional<LeftJoinOptimizationProposal> createNewProposalFromFocusNode(LeftJoinNode focusNode) {
        LeftJoinOptimizationProposal proposal = new LeftJoinOptimizationProposalImpl(focusNode);
        return Optional.of(proposal);
    }

    @Override
    protected ImmutableList<SimpleNodeCentricExecutor<LeftJoinNode, LeftJoinOptimizationProposal>> getExecutors() {
        return executors;
    }
}
