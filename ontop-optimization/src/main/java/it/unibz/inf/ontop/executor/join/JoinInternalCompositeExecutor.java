package it.unibz.inf.ontop.executor.join;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalCompositeExecutor;
import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalExecutor;
import it.unibz.inf.ontop.pivotalrepr.InnerJoinNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;

import java.util.Optional;

/**
 * TODO: explain
 */
@Singleton
public class JoinInternalCompositeExecutor
        extends SimpleNodeCentricInternalCompositeExecutor<InnerJoinNode, InnerJoinOptimizationProposal>
        implements InnerJoinExecutor {

    private final ImmutableList<SimpleNodeCentricInternalExecutor<InnerJoinNode, InnerJoinOptimizationProposal>> executors;

    @Inject
    private JoinInternalCompositeExecutor(JoinBooleanExpressionExecutor expressionExecutor,
                                          RedundantSelfJoinExecutor selfJoinExecutor,
                                          RedundantJoinFKExecutor fkExecutor) {
        ImmutableList.Builder<SimpleNodeCentricInternalExecutor<InnerJoinNode, InnerJoinOptimizationProposal>>
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
    protected ImmutableList<SimpleNodeCentricInternalExecutor<InnerJoinNode, InnerJoinOptimizationProposal>> getExecutors() {
        return executors;
    }
}
