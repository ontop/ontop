package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

/**
 * Unifies the types of the arguments by taking their common denominator.
 */
public class UnifierTermTypeInferenceRule extends AbstractTermTypeInferenceRule {

    /**
     * We only infer a type when all the types of the arguments are known.
     */
    @Override
    protected Optional<TermType> reduceInferredTypes(ImmutableList<Optional<TermType>> argumentTypes) {
        if (argumentTypes.stream().allMatch(Optional::isPresent)) {
            return argumentTypes.stream()
                    .map(Optional::get)
                    .reduce(TermType::getCommonDenominator);
        }
        return Optional.empty();
    }


}
