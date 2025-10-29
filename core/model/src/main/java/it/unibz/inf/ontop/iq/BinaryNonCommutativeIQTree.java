package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;
import it.unibz.inf.ontop.iq.visit.IQTreeVisitor;

/**
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface BinaryNonCommutativeIQTree extends CompositeIQTree<BinaryNonCommutativeOperatorNode> {

    IQTree getLeftChild();

    IQTree getRightChild();

    @Override
    default  <T> T acceptVisitor(IQTreeVisitor<T> visitor) {
        return getRootNode().acceptVisitor(this, visitor, getLeftChild(), getRightChild());
    }
}
