package it.unibz.inf.ontop.executor.leftjoin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.executor.SimpleNodeCentricExecutor;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.DataAtom;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.LeftJoinOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.validation.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.sql.DatabaseRelationDefinition;

import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;

/**
 * Tries to transform the left join into a inner join node
 * TODO: describe them
 *
 * TODO: explicit the assumptions
 *
 */
@Singleton
public class LeftToInnerJoinExecutor implements SimpleNodeCentricExecutor<LeftJoinNode, LeftJoinOptimizationProposal> {

    @Inject
    private LeftToInnerJoinExecutor() {
    }

    @Override
    public NodeCentricOptimizationResults<LeftJoinNode> apply(LeftJoinOptimizationProposal proposal,
                                                              IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        LeftJoinNode leftJoinNode = proposal.getFocusNode();

        QueryNode leftChild = query.getChild(leftJoinNode, LEFT)
                .orElseThrow(() -> new InvalidIntermediateQueryException("A LJ must have a left child"));

        QueryNode rightChild = query.getChild(leftJoinNode, RIGHT)
                .orElseThrow(() -> new InvalidIntermediateQueryException("A LJ must have a right child"));

        /*
         * No normalization (a DataNode is expected on the left)
         */
        if (!(leftChild instanceof DataNode))
            return new NodeCentricOptimizationResultsImpl<>(query, leftJoinNode);

        DataNode leftDataNode = (DataNode) leftChild;

        if (rightChild instanceof DataNode) {
            return optimizeRightDataNode(leftJoinNode, query, treeComponent, leftDataNode, (DataNode) rightChild);
        } else if (rightChild instanceof UnionNode) {
            return optimizeRightUnion(leftJoinNode, query, treeComponent, leftDataNode, (UnionNode) rightChild);
        }
        /*
         * No normalization
         *
         * TODO: support more cases
         */
        else {
            return new NodeCentricOptimizationResultsImpl<>(query, leftJoinNode);
        }
    }


    private  NodeCentricOptimizationResults<LeftJoinNode> optimizeRightDataNode(LeftJoinNode leftJoinNode,
                                                                                IntermediateQuery query,
                                                                                QueryTreeComponent treeComponent,
                                                                                DataNode leftChild, DataNode rightChild) {


        throw new RuntimeException("TODO: continue");

    }

    private  NodeCentricOptimizationResults<LeftJoinNode> optimizeRightUnion(LeftJoinNode leftJoinNode,
                                                                             IntermediateQuery query,
                                                                             QueryTreeComponent treeComponent,
                                                                             DataNode leftDataNode, UnionNode rightChild) {
        throw new RuntimeException("TODO: support the normalization with a right union node");
    }
}
