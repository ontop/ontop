package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;

/**
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface UnaryIQTree extends CompositeIQTree<UnaryOperatorNode> {

    IQTree getChild();
}
