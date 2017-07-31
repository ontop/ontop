package it.unibz.inf.ontop.iq.executor.truenode;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.node.NodeTransformationProposal;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.TrueNode;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.TrueNodeRemovalProposal;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.Optional;

public class TrueNodeRemovalExecutorImpl implements TrueNodeRemovalExecutor {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private TrueNodeRemovalExecutorImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public NodeCentricOptimizationResults<TrueNode> apply(TrueNodeRemovalProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        TrueNode originalFocusNode = proposal.getFocusNode();

        return reactToTrueChildNodeRemovalProposal(query, originalFocusNode, treeComponent);
    }

    private NodeCentricOptimizationResults reactToTrueChildNodeRemovalProposal(IntermediateQuery query, TrueNode trueNode, QueryTreeComponent treeComponent)
            throws EmptyQueryException {

        QueryNode originalParentNode = query.getParent(trueNode).orElseThrow(EmptyQueryException::new);

        Optional<QueryNode> optionalOriginalNextSibling = query.getNextSibling(trueNode);

        NodeTransformationProposal transformationProposal = originalParentNode.reactToTrueChildRemovalProposal(query, trueNode);

        switch (transformationProposal.getState()) {
            case NO_LOCAL_CHANGE:
                treeComponent.removeSubTree(trueNode);
                return new NodeCentricOptimizationResultsImpl<>(query, optionalOriginalNextSibling, Optional.of(originalParentNode));
            case REPLACE_BY_UNIQUE_NON_EMPTY_CHILD:
                treeComponent.removeSubTree(trueNode);
                treeComponent.removeOrReplaceNodeByUniqueChild(originalParentNode);
                return new NodeCentricOptimizationResultsImpl<>(query, transformationProposal.getOptionalNewNodeOrReplacingChild());
            case DECLARE_AS_TRUE:
                TrueNode newTrueNode = iqFactory.createTrueNode();
                treeComponent.replaceSubTree(originalParentNode, newTrueNode);
                return new NodeCentricOptimizationResultsImpl<>(query, query.getNextSibling(newTrueNode), query.getParent(newTrueNode));
            case REPLACE_BY_NEW_NODE:
                treeComponent.removeSubTree(trueNode);
                QueryNode replacingNode = transformationProposal.getOptionalNewNodeOrReplacingChild()
                                .orElseThrow(() -> new IllegalArgumentException("A replacing node should be provided"));
                treeComponent.replaceNode(originalParentNode, replacingNode);
                return new NodeCentricOptimizationResultsImpl<>(query,replacingNode);
            default:
                throw new RuntimeException("Unexpected state: " + transformationProposal.getState());
        }
    }
}
