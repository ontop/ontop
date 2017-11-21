package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.QueryNode;

public interface CompositeIQ<N extends QueryNode> extends IQ {

    @Override
    N getRootNode();
}
