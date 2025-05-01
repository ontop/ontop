package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

/**
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface BinaryNonCommutativeIQTree extends CompositeIQTree<BinaryNonCommutativeOperatorNode> {

    IQTree getLeftChild();

    IQTree getRightChild();

    @Override
    default IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return getRootNode().acceptTransformer(this, transformer, getLeftChild(), getRightChild());
    }

    @Override
    default  <T> T acceptVisitor(IQVisitor<T> visitor) {
        return getRootNode().acceptVisitor(this, visitor, getLeftChild(), getRightChild());
    }
}
