package it.unibz.inf.ontop.model.type.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.TermType;
import it.unibz.inf.ontop.model.type.TermTypeException;

import java.util.Optional;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER;
import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.NUMERIC_TYPES;
import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER_TYPES;
import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;


public class NumericTermTypeInferenceRule extends UnifierTermTypeInferenceRule {

    /**
     * Checks that all the terms are numeric
     */
    protected void doAdditionalChecks(ImmutableList<Optional<TermType>> argumentTypes)
            throws TermTypeException {
        IntStream.range(0, argumentTypes.size())
                .forEach(i ->  {
                    if(!argumentTypes.get(i)
                            .map(t -> NUMERIC_TYPES.contains(t.getColType()))
                            .orElse(true)) {
                        // TODO: refactor the exception
                        throw new TermTypeException(null, argumentTypes.get(i).get());
                    }
                });
    }

    /**
     * Only base numeric types (double, float, decimal and integer) can be returned by numeric functions and operators
     */
    @Override
    protected Optional<TermType> postprocessInferredType(Optional<TermType> optionalTermType) {
        return optionalTermType
                .map(t -> INTEGER_TYPES.contains(t.getColType()) ? DATA_FACTORY.getTermType(INTEGER) : t);
    }
}
