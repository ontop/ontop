package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;

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
}
