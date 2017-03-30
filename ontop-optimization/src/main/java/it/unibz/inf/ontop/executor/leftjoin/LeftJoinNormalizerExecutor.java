package it.unibz.inf.ontop.executor.leftjoin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.executor.SimpleNodeCentricExecutor;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.model.DataAtom;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.LeftJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.validation.InvalidIntermediateQueryException;

import static it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;

/**
 * Tries to normalize according to 4 interesting forms:
 * TODO: describe them
 *
 * TODO: explicit the assumptions
 *
 */
@Singleton
public class LeftJoinNormalizerExecutor implements SimpleNodeCentricExecutor<LeftJoinNode, LeftJoinOptimizationProposal> {

    @Inject
    private LeftJoinNormalizerExecutor() {
    }

    @Override
    public NodeCentricOptimizationResults<LeftJoinNode> apply(LeftJoinOptimizationProposal proposal,
                                                              IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        LeftJoinNode initialLeftJoinNode = proposal.getFocusNode();
        LeftJoinNode updatedLeftJoinNode = normalize(initialLeftJoinNode, query, treeComponent);
        return new NodeCentricOptimizationResultsImpl<>(query, updatedLeftJoinNode);
    }

    /**
     *
     * In case of no-normalization, returns the same left-join node
     */
    private LeftJoinNode normalize(LeftJoinNode leftJoinNode, IntermediateQuery query,
                                   QueryTreeComponent treeComponent) {
        QueryNode leftChild = query.getChild(leftJoinNode, LEFT)
                .orElseThrow(() -> new InvalidIntermediateQueryException("A LJ must have a left child"));

        QueryNode rightChild = query.getChild(leftJoinNode, RIGHT)
                .orElseThrow(() -> new InvalidIntermediateQueryException("A LJ must have a right child"));

        /*
         * No normalization (a DataNode is expected on the left)
         */
        if (!(leftChild instanceof DataNode))
            return leftJoinNode;

        DataNode leftDataNode = (DataNode) leftChild;

        if (rightChild instanceof DataNode) {
            return normalizeWithRightDataNode(leftJoinNode, query, treeComponent, leftDataNode, (DataNode) rightChild);
        } else if (rightChild instanceof UnionNode) {
            return normalizeWithRightUnion(leftJoinNode, query, treeComponent, leftDataNode, (UnionNode) rightChild);
        }
        /*
         * No normalization
         */
        else {
            return leftJoinNode;
        }
    }


    private LeftJoinNode normalizeWithRightDataNode(LeftJoinNode leftJoinNode, IntermediateQuery query,
                                                    QueryTreeComponent treeComponent, DataNode leftChild,
                                                    DataNode rightChild) {
        DataAtom leftProjectionAtom = leftChild.getProjectionAtom();
        DataAtom rightProjectionAtom = rightChild.getProjectionAtom();

        AtomPredicate leftPredicate = leftProjectionAtom.getPredicate();
        AtomPredicate rightPredicate = rightProjectionAtom.getPredicate();

        /*
         * First: look for unique constraints
         */
        if (leftPredicate.equals(rightPredicate)) {

        }

        throw new RuntimeException("TODO: continue");

    }

    private LeftJoinNode normalizeWithRightUnion(LeftJoinNode leftJoinNode, IntermediateQuery query,
                                                 QueryTreeComponent treeComponent, DataNode leftDataNode,
                                                 UnionNode rightChild) {
        throw new RuntimeException("TODO: support the normalization with a right union node");
    }
}
