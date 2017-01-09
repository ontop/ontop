package it.unibz.inf.ontop.executor.leftjoin;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalCompositeExecutor;
import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalExecutor;
import it.unibz.inf.ontop.pivotalrepr.LeftJoinNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.LeftJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.LeftJoinOptimizationProposalImpl;

import java.util.Optional;

/**
 * TODO: explain
 */
@Singleton
public class LeftJoinInternalCompositeExecutor extends SimpleNodeCentricInternalCompositeExecutor<LeftJoinNode,
        LeftJoinOptimizationProposal> implements LeftJoinExecutor {

    private final ImmutableList<SimpleNodeCentricInternalExecutor<LeftJoinNode, LeftJoinOptimizationProposal>> executors;

    @Inject
    private LeftJoinInternalCompositeExecutor(RedundantSelfLeftJoinExecutor selfLeftJoinExecutor,
                                              ForeignKeyLeftJoinExecutor fkExecutor) {
        ImmutableList.Builder<SimpleNodeCentricInternalExecutor<LeftJoinNode, LeftJoinOptimizationProposal>> executorBuilder = ImmutableList.builder();

        //executorBuilder.add(new LeftJoinBooleanExpressionExecutor());
        executorBuilder.add(selfLeftJoinExecutor);
        executorBuilder.add(fkExecutor);

        executors = executorBuilder.build();
    }

    @Override
    protected Optional<LeftJoinOptimizationProposal> createNewProposalFromFocusNode(LeftJoinNode focusNode) {
        LeftJoinOptimizationProposal proposal = new LeftJoinOptimizationProposalImpl(focusNode);
        return Optional.of(proposal);
    }

    @Override
    protected ImmutableList<SimpleNodeCentricInternalExecutor<LeftJoinNode, LeftJoinOptimizationProposal>> getExecutors() {
        return executors;
    }
}
