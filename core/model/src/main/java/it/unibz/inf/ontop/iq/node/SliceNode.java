package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

import java.util.Optional;

/**
 * See {@link IntermediateQueryFactory#createSliceNode} for creating a new instance.
 */
public interface SliceNode extends QueryModifierNode {

    /**
     * Beginning of the slice
     */
    long getOffset();

    /**
     * Length of the slice
     */
    Optional<Long> getLimit();

    @Override
    default IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformSlice(tree, this, child);
    }

    @Override
    default <T> T acceptVisitor(IQTree tree, IQVisitor<T> visitor, IQTree child) {
        return visitor.visitSlice(tree, this, child);
    }

}
