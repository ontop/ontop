package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;

public interface LeafIQ extends IQ, ExplicitVariableProjectionNode {

    @Override
    LeafIQ getRootNode();
}
