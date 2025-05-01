package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

/**
 * See {@link IntermediateQueryFactory#createDistinctNode()} for creating a new instance.
 */
public interface DistinctNode extends QueryModifierNode {

    @Override
    default IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformDistinct(tree, this, child);
    }

    @Override
    default <T> T acceptVisitor(IQTree tree, IQVisitor<T> visitor, IQTree child) {
        return visitor.visitDistinct(tree, this, child);
    }

}
