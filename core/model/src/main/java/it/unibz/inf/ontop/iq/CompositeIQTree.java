package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.QueryNode;

/**
 * For composite trees, isEquivalentTo(o) == equals(o)
 */
public interface CompositeIQTree<N extends QueryNode> extends IQTree {

    @Override
    N getRootNode();

}
