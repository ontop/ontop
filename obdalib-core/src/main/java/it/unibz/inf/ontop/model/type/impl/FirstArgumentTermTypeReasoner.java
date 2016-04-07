package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

/**
 * TODO: explain
 */
public class FirstArgumentTermTypeReasoner extends AbstractTermTypeReasoner {

    @Override
    protected Optional<TermType> deduceType(ImmutableList<Optional<TermType>> argumentTypes) {
        if (argumentTypes.isEmpty()) {
            throw new IllegalStateException("At least one argument is required by the FirstArgumentTermTypeReasoner");
        }
        return argumentTypes.get(0);
    }
}
