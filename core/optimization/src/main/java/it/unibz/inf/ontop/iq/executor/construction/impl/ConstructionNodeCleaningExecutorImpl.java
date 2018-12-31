package it.unibz.inf.ontop.iq.executor.construction.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.executor.construction.ConstructionNodeCleaningExecutor;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.ConstructionNodeCleaningProposal;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.Optional;

/**
 * TODO: should we keep it since query modifiers were removed from the construction node?
 */
public class ConstructionNodeCleaningExecutorImpl implements ConstructionNodeCleaningExecutor {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private ConstructionNodeCleaningExecutorImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public NodeCentricOptimizationResults<ConstructionNode> apply(ConstructionNodeCleaningProposal proposal,
                                                                  IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        ConstructionNode focusNode = proposal.getFocusNode();
        QueryNode childSubtreeRoot = proposal.getChildSubtreeRoot();
        if(proposal.deleteConstructionNodeChain()){
            return deleteConstructionNodeChain(query, treeComponent, focusNode, childSubtreeRoot);
        }
        return flattenConstructionNodeChain(query, treeComponent, focusNode, childSubtreeRoot);
    }

    private NodeCentricOptimizationResults<ConstructionNode> deleteConstructionNodeChain(IntermediateQuery query,
                                                                                         QueryTreeComponent treeComponent, ConstructionNode focusNode, QueryNode childSubtreeRoot) {

        IntermediateQuery snapshot = query.createSnapshot();
        treeComponent.replaceSubTree(focusNode, childSubtreeRoot);
        treeComponent.addSubTree(snapshot, childSubtreeRoot, childSubtreeRoot);
        return new NodeCentricOptimizationResultsImpl(query, Optional.of(childSubtreeRoot));
    }

    private NodeCentricOptimizationResults<ConstructionNode> flattenConstructionNodeChain(IntermediateQuery query,
                                                                                          QueryTreeComponent treeComponent,
                                                                                          ConstructionNode focusNode,
                                                                                          QueryNode childSubtreeRoot) {
        IntermediateQuery snapshot = query.createSnapshot();
        ConstructionNode replacingNode = iqFactory.createConstructionNode(
                focusNode.getVariables(),
                focusNode.getSubstitution());

        treeComponent.replaceSubTree(focusNode, replacingNode);
        treeComponent.addChild(replacingNode, childSubtreeRoot, Optional.empty(), false);
        treeComponent.addSubTree(snapshot, childSubtreeRoot, childSubtreeRoot);

        return new NodeCentricOptimizationResultsImpl<>(query, replacingNode);

    }
}
