package it.unibz.inf.ontop.executor;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.SimpleNodeCentricOptimizationProposal;

public abstract class SimpleNodeCentricInternalCompositeExecutor<
            N extends QueryNode,
            P extends SimpleNodeCentricOptimizationProposal<N>>
        extends NodeCentricInternalCompositeExecutor<N, NodeCentricOptimizationResults<N>, P> {

    @Override
    protected abstract ImmutableList<SimpleNodeCentricInternalExecutor<N, P>> createExecutors();
}
