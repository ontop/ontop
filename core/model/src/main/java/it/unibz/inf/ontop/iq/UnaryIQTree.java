package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;

public interface UnaryIQTree extends CompositeIQTree<UnaryOperatorNode> {

    IQTree getChild();
}
