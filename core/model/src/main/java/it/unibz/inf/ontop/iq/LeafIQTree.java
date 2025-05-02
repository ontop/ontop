package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;

public interface LeafIQTree extends IQTree, ExplicitVariableProjectionNode {

    @Override
    LeafIQTree getRootNode();

}
