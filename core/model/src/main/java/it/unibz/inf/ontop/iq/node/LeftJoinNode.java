package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

/**
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface LeftJoinNode extends JoinLikeNode, BinaryNonCommutativeOperatorNode {

    @Override
    default <T> T acceptVisitor(BinaryNonCommutativeIQTree tree, IQVisitor<T> visitor, IQTree leftChild, IQTree rightChild) {
        return visitor.transformLeftJoin(tree, this, leftChild, rightChild);
    }
}
