package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

import java.util.OptionalLong;

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
    OptionalLong getLimit();

    @Override
    default <T> T acceptVisitor(UnaryIQTree tree, IQVisitor<T> visitor, IQTree child) {
        return visitor.transformSlice(tree, this, child);
    }
}
