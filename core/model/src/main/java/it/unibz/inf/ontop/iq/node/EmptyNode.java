package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

/**
 * Temporary QueryNode that says that replace a non-satisfied sub-tree.
 *
 * Is expected to remove quickly.
 *
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface EmptyNode extends LeafIQTree {
    @Override
    default EmptyNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    default IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return transformer.transformEmpty(this);
    }

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.visitEmpty(this);
    }
}
