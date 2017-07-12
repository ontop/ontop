package it.unibz.inf.ontop.executor.construction.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.executor.construction.ConstructionNodeRemovalExecutor;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.ConstructionNodeRemovalProposal;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

public class ConstructionNodeRemovalExecutorImpl implements ConstructionNodeRemovalExecutor {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private ConstructionNodeRemovalExecutorImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public NodeCentricOptimizationResults<ConstructionNode> apply(ConstructionNodeRemovalProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        ConstructionNode focusNode = proposal.getFocusNode();
        QueryNode childSubtreeRoot = proposal.getChildSubtreeRoot();
        ImmutableSubstitution substitution = proposal.getSubstitution();
        if(proposal.deleteConstructionNodeChain()){
            return deleteConstructionNodeChain(query, treeComponent, focusNode, childSubtreeRoot);
        }
        return flattenConstructionNodeChain(query, treeComponent, focusNode, childSubtreeRoot, substitution);
    }

    private NodeCentricOptimizationResults<ConstructionNode> deleteConstructionNodeChain(IntermediateQuery query, QueryTreeComponent treeComponent, ConstructionNode focusNode, QueryNode childSubtreeRoot) {

        IntermediateQuery snapshot = query.createSnapshot();
        treeComponent.replaceSubTree(focusNode, childSubtreeRoot);
        treeComponent.addSubTree(snapshot, childSubtreeRoot, childSubtreeRoot);
        return new NodeCentricOptimizationResultsImpl(query, Optional.of(childSubtreeRoot));
    }

    private NodeCentricOptimizationResults<ConstructionNode> flattenConstructionNodeChain(IntermediateQuery query,
                                                                                          QueryTreeComponent treeComponent,
                                                                                          ConstructionNode focusNode,
                                                                                          QueryNode childSubtreeRoot,
                                                                                          ImmutableSubstitution substitution) {
        IntermediateQuery snapshot = query.createSnapshot();
        ConstructionNode replacingNode = iqFactory.createConstructionNode(
                focusNode.getVariables(),
                substitution
        );

        treeComponent.replaceSubTree(focusNode, replacingNode);
        treeComponent.addChild(replacingNode, childSubtreeRoot, Optional.empty(), false);
        treeComponent.addSubTree(snapshot, childSubtreeRoot, childSubtreeRoot);

        return new NodeCentricOptimizationResultsImpl<>(query, replacingNode);

    }
}
