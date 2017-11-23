package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.IQTree;

/**
 * Has ONE child
 */
public interface UnaryOperatorNode extends QueryNode {

    IQTree liftBinding(IQTree liftedChildIQTree);
}
