package it.unibz.inf.ontop.iq.executor.union.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.executor.union.FlattenUnionExecutor;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.proposal.FlattenUnionProposal;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.Optional;

public class FlattenUnionExecutorImpl implements FlattenUnionExecutor {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private FlattenUnionExecutorImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    /**
     * Replace the child subtrees of the focus node
     **/
    @Override
    public NodeCentricOptimizationResults<UnionNode> apply(FlattenUnionProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        UnionNode focusNode = proposal.getFocusNode();
        IntermediateQuery snapShot = query.createSnapshot();

        query.getChildren(focusNode).stream()
                .forEach(n -> treeComponent.removeSubTree(n));

        ImmutableSet<QueryNode> subqueryRoots = proposal.getSubqueryRoots();
        subqueryRoots.forEach(n -> treeComponent.addChild(
                focusNode,
                n,
                Optional.empty(),
                false
        ));
        subqueryRoots.forEach(n -> treeComponent.addSubTree(
                snapShot,
                n,
                n
        ));
        return new NodeCentricOptimizationResultsImpl<>(query, focusNode);
    }

}
