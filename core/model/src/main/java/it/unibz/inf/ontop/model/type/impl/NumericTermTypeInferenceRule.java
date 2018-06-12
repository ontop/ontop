package it.unibz.inf.ontop.model.type.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.ConcreteNumericRDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;


public class NumericTermTypeInferenceRule extends AbstractTermTypeInferenceRule {

    /**
     * We only infer a type when all the types of the arguments are known.
     */
    @Override
    protected Optional<TermTypeInference> reduceInferredTypes(ImmutableList<Optional<TermTypeInference>> argumentTypes) {
        if (argumentTypes.stream()
                .allMatch(t -> t.flatMap(TermTypeInference::getTermType).isPresent())) {
            return argumentTypes.stream()
                    .map(Optional::get)
                    .map(TermTypeInference::getTermType)
                    .map(Optional::get)
                    .map(t -> (ConcreteNumericRDFDatatype) t)
                    .reduce(ConcreteNumericRDFDatatype::getCommonPropagatedOrSubstitutedType)
                    .map(t -> (TermType) t)
                    .map(TermTypeInference::declareTermType);
        }
        return Optional.empty();
    }
}
