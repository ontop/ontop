package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

/**
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface UnaryIQTree extends CompositeIQTree<UnaryOperatorNode> {

    IQTree getChild();

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor) {
        return getRootNode().acceptVisitor(this, visitor, getChild());
    }

}
