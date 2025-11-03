package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.NaryOperatorNode;
import it.unibz.inf.ontop.iq.visit.IQTreeVisitor;

/**
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface NaryIQTree extends CompositeIQTree<NaryOperatorNode> {

    @Override
    default <T> T acceptVisitor(IQTreeVisitor<T> visitor) {
        return getRootNode().acceptVisitor(this, visitor, getChildren());
    }
}
