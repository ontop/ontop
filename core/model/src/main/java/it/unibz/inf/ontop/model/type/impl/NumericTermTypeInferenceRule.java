package it.unibz.inf.ontop.model.type.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.IncompatibleTermException;
import it.unibz.inf.ontop.model.type.ConcreteNumericRDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;
import java.util.stream.IntStream;


public class NumericTermTypeInferenceRule extends AbstractTermTypeInferenceRule {

    /**
     * We only infer a type when all the types of the arguments are known.
     */
    @Override
    protected Optional<TermType> reduceInferredTypes(ImmutableList<Optional<TermType>> argumentTypes) {
        if (argumentTypes.stream().allMatch(Optional::isPresent)) {
            return argumentTypes.stream()
                    .map(Optional::get)
                    .map(t -> (ConcreteNumericRDFDatatype) t)
                    .reduce(ConcreteNumericRDFDatatype::getCommonPropagatedOrSubstitutedType)
                    .map(t -> (TermType) t);
        }
        return Optional.empty();
    }
}
