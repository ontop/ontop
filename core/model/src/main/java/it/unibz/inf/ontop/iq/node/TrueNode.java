package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

/**
 * See {@link IntermediateQueryFactory#createTrueNode()} for creating a new instance.
 */
public interface TrueNode extends LeafIQTree {

    @Override
    default TrueNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    default IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return transformer.transformTrue(this);
    }

    @Override
    default <T> IQTree acceptTransformer(IQTreeExtendedTransformer<T> transformer, T context) {
        return transformer.transformTrue(this, context);
    }

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.visitTrue(this);
    }

}
