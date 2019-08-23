package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * Temporary QueryNode that says that replace a non-satisfied sub-tree.
 *
 * Is expected to remove quickly.
 */
public interface EmptyNode extends LeafIQTree {

    @Override
    EmptyNode clone();

    @Override
    EmptyNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;
}
