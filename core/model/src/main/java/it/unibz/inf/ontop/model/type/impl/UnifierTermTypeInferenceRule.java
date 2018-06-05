package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeInference;

import java.util.Optional;

/**
 * Unifies the types of the arguments by taking their common denominator.
 */
public class UnifierTermTypeInferenceRule extends AbstractTermTypeInferenceRule {

    /**
     * We only infer a type when all the types of the arguments are known.
     */
    @Override
    protected TypeInference reduceInferredTypes(ImmutableList<TypeInference> argumentTypes) {
        if (argumentTypes.stream()
                .map(TypeInference::getTermType)
                .allMatch(Optional::isPresent)) {
            return argumentTypes.stream()
                    .map(TypeInference::getTermType)
                    .map(Optional::get)
                    .reduce(TermType::getCommonDenominator)
                    .map(TypeInference::declareTermType)
                    .orElseGet(TypeInference::declareNotDetermined);
        }
        return TypeInference.declareNotDetermined();
    }


}
