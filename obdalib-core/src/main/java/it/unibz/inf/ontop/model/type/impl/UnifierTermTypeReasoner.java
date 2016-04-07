package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;
import java.util.stream.Collector;

/**
 * Unifies the types of the arguments by taking their common denominator.
 */
public class UnifierTermTypeReasoner extends AbstractTermTypeReasoner {

    @Override
    protected Optional<TermType> deduceType(ImmutableList<Optional<TermType>> argumentTypes) {
        return argumentTypes.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collector.of(
                        // Supplier
                        Optional::empty,
                        // Accumulator
                        (optType, t2) -> optType
                                .map(t1 -> unifyTypes(t1, t2))
                                .orElseGet(() -> Optional.of(t2)),
                        // Combiner
                        (optType1, optType2) -> optType1
                                .map(t1 -> optType2
                                        .map(t2 -> unifyTypes(t1, t2))
                                        .orElse(optType1))
                                .orElse(optType2)));
    }

    /**
     * Can be overwritten
     */
    protected Optional<TermType> unifyTypes(TermType type1, TermType type2) {
        return type1.getCommonDenominator(type2);
    }

}
