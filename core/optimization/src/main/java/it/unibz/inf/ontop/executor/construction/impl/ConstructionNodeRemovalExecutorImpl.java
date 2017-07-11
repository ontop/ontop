package it.unibz.inf.ontop.executor.construction.impl;

import it.unibz.inf.ontop.executor.construction.ConstructionNodeRemovalExecutor;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.ConstructionNodeRemovalProposal;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.Optional;

public class ConstructionNodeRemovalExecutorImpl implements ConstructionNodeRemovalExecutor {

    @Override
    public NodeCentricOptimizationResults<ConstructionNode> apply(ConstructionNodeRemovalProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        ConstructionNode focusNode = proposal.getFocusNode();
        ConstructionNode replacingNode = proposal.getReplacingNode();
        QueryNode childSubtreeRoot = proposal.getChildSubtreeRoot();

        IntermediateQuery snapshot = query.createSnapshot();

        treeComponent.replaceSubTree(focusNode, replacingNode);
        treeComponent.addChild(replacingNode, childSubtreeRoot, Optional.empty(), false);
        treeComponent.addSubTree(snapshot, childSubtreeRoot, childSubtreeRoot);

        return new NodeCentricOptimizationResultsImpl<>(query, replacingNode);
    }
}
