package it.unibz.inf.ontop.iq.node;

import java.util.Optional;

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
    SliceNode clone();
}
