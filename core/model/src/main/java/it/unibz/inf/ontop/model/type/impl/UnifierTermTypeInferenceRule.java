package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

/**
 * Unifies the types of the arguments by taking their common denominator.
 */
public class UnifierTermTypeInferenceRule extends AbstractTermTypeInferenceRule {

    /**
     * We only infer a type when all the types of the arguments are known.
     */
    @Override
    protected Optional<TermTypeInference> reduceInferredTypes(ImmutableList<Optional<TermTypeInference>> argumentTypes) {
        if (argumentTypes.stream()
                .map(o -> o.flatMap(TermTypeInference::getTermType))
                .allMatch(Optional::isPresent)) {
            return argumentTypes.stream()
                    .map(Optional::get)
                    .map(TermTypeInference::getTermType)
                    .map(Optional::get)
                    .reduce(TermType::getCommonDenominator)
                    .map(TermTypeInference::declareTermType);
        }
        return Optional.empty();
    }


}
