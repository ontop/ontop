package it.unibz.inf.ontop.executor.union.impl;

import it.unibz.inf.ontop.executor.union.FlattenUnionExecutor;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.proposal.FlattenUnionProposal;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.Optional;

public class FlattenUnionExecutorImpl implements FlattenUnionExecutor{


    /** Replace the child subtrees of the focus node **/
    @Override
    public NodeCentricOptimizationResults<UnionNode> apply(FlattenUnionProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        UnionNode focusNode = proposal.getFocusNode();
        query.getChildren(focusNode).stream()
               .forEach(n -> treeComponent.removeSubTree(n));
        proposal.getReplacingChildSubtreeRoots().stream()
                .forEach(n -> treeComponent.addChild(
                        focusNode,
                        n,
                        Optional.empty(),
                        false
                ));
        return new NodeCentricOptimizationResultsImpl<>(query, focusNode);
    }
}
