package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * See {@link IntermediateQueryFactory#createTrueNode()} for creating a new instance.
 */
public interface TrueNode extends LeafIQTree {
    @Override
    TrueNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;
}
