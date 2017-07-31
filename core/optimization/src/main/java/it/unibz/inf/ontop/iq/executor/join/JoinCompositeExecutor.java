package it.unibz.inf.ontop.iq.executor.join;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.iq.executor.SimpleNodeCentricCompositeExecutor;
import it.unibz.inf.ontop.iq.executor.SimpleNodeCentricExecutor;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.proposal.InnerJoinOptimizationProposal;
import it.unibz.inf.ontop.iq.proposal.impl.InnerJoinOptimizationProposalImpl;

import java.util.Optional;

/**
 * TODO: explain
 */
@Singleton
public class JoinCompositeExecutor
        extends SimpleNodeCentricCompositeExecutor<InnerJoinNode, InnerJoinOptimizationProposal>
        implements InnerJoinExecutor {

    private final ImmutableList<SimpleNodeCentricExecutor<InnerJoinNode, InnerJoinOptimizationProposal>> executors;

    @Inject
    private JoinCompositeExecutor(JoinBooleanExpressionExecutor expressionExecutor,
                                  UCRedundantSelfJoinExecutor ucSelfJoinExecutor,
                                  LooseFDRedundantSelfJoinExecutor nufcSelfJoinExecutor,
                                  RedundantJoinFKExecutor fkExecutor) {
        ImmutableList.Builder<SimpleNodeCentricExecutor<InnerJoinNode, InnerJoinOptimizationProposal>>
                executorBuilder = ImmutableList.builder();
        executorBuilder.add(expressionExecutor);
        executorBuilder.add(ucSelfJoinExecutor);
        executorBuilder.add(fkExecutor);
        executorBuilder.add(nufcSelfJoinExecutor);

        executors = executorBuilder.build();
    }

    @Override
    protected Optional<InnerJoinOptimizationProposal> createNewProposalFromFocusNode(InnerJoinNode focusNode) {
        InnerJoinOptimizationProposal proposal = new InnerJoinOptimizationProposalImpl(focusNode);
        return Optional.of(proposal);
    }

    @Override
    protected ImmutableList<SimpleNodeCentricExecutor<InnerJoinNode, InnerJoinOptimizationProposal>> getExecutors() {
        return executors;
    }
}
