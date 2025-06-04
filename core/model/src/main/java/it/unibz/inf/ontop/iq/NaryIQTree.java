package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.NaryOperatorNode;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

/**
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface NaryIQTree extends CompositeIQTree<NaryOperatorNode> {

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor) {
        return getRootNode().acceptVisitor(this, visitor, getChildren());
    }
}
