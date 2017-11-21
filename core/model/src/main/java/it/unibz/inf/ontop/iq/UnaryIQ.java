package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;

public interface UnaryIQ extends CompositeIQ<UnaryOperatorNode> {

    IQ getChild();
}
