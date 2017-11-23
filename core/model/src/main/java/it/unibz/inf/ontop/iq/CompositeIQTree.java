package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.QueryNode;

public interface CompositeIQTree<N extends QueryNode> extends IQTree {

    @Override
    N getRootNode();
}
