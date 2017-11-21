package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.IQ;

/**
 * Has ONE child
 */
public interface UnaryOperatorNode extends QueryNode {

    IQ liftBinding(IQ liftedChildIQ);
}
