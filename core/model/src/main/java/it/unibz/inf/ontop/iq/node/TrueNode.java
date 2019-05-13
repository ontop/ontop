package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface TrueNode extends LeafIQTree {
    @Override
    TrueNode clone();

    @Override
    TrueNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;
}
