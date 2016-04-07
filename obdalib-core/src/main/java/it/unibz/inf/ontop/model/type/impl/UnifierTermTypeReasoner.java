package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

/**
 * Unifies the types of the arguments by taking their common denominator.
 */
public class UnifierTermTypeReasoner extends AbstractTermTypeReasoner {

    @Override
    protected Optional<TermType> deduceType(ImmutableList<Optional<TermType>> argumentTypes) {
        return argumentTypes.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(this::unifyTypes);
    }

    /**
     * Can be overwritten
     */
    protected TermType unifyTypes(TermType type1, TermType type2) {
        return type1.getCommonDenominator(type2)
                // This should have detected before
                .orElseThrow(() -> new IllegalStateException("Try to \"unify\" incompatible types (" + type1
                        + " and " + type2 +  ". This should have been detected before"));
    }

}
