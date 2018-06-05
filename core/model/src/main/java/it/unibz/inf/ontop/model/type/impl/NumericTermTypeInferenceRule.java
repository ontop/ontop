package it.unibz.inf.ontop.model.type.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.ConcreteNumericRDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeInference;

import java.util.Optional;


public class NumericTermTypeInferenceRule extends AbstractTermTypeInferenceRule {

    /**
     * We only infer a type when all the types of the arguments are known.
     */
    @Override
    protected TypeInference reduceInferredTypes(ImmutableList<TypeInference> argumentTypes) {
        if (argumentTypes.stream()
                .allMatch(t -> t.getTermType().isPresent())) {
            return argumentTypes.stream()
                    .map(TypeInference::getTermType)
                    .map(Optional::get)
                    .map(t -> (ConcreteNumericRDFDatatype) t)
                    .reduce(ConcreteNumericRDFDatatype::getCommonPropagatedOrSubstitutedType)
                    .map(t -> (TermType) t)
                    .map(TypeInference::declareTermType)
                    .orElseGet(TypeInference::declareNotDetermined);
        }
        return TypeInference.declareNotDetermined();
    }
}
