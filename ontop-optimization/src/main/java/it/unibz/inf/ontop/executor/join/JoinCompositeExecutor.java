package it.unibz.inf.ontop.executor.join;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.executor.SimpleNodeCentricCompositeExecutor;
import it.unibz.inf.ontop.executor.SimpleNodeCentricExecutor;
import it.unibz.inf.ontop.pivotalrepr.InnerJoinNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;

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
                                  UCRedundantSelfJoinExecutor selfJoinExecutor,
                                  RedundantJoinFKExecutor fkExecutor) {
        ImmutableList.Builder<SimpleNodeCentricExecutor<InnerJoinNode, InnerJoinOptimizationProposal>>
                executorBuilder = ImmutableList.builder();
        executorBuilder.add(expressionExecutor);
        executorBuilder.add(selfJoinExecutor);
        executorBuilder.add(fkExecutor);

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
