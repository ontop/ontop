package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

public interface LeafIQTree extends IQTree, ExplicitVariableProjectionNode {

    @Override
    LeafIQTree getRootNode();

    @Override
    LeafIQTree acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;
}
