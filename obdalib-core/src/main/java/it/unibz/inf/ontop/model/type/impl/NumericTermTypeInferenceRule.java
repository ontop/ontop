package it.unibz.inf.ontop.model.type.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.Term;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeException;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.NUMERIC_TYPES;
import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER_TYPES;


public class NumericTermTypeInferenceRule extends UnifierTermTypeInferenceRule {

    /**
     * Checks that all the terms are numeric
     */
    protected void doAdditionalChecks(List<Term> terms, ImmutableList<Optional<TermType>> argumentTypes)
            throws TermTypeException {
        IntStream.range(0, terms.size())
                .forEach(i ->  {
                    if(!argumentTypes.get(i)
                            .map(NUMERIC_TYPES::contains)
                            .orElse(true)) {
                        // TODO: refactor the exception
                        throw new TermTypeException(terms.get(i), null, argumentTypes.get(i).get());
                    }
                });
    }

    /**
     * Only base numeric types (double, float, decimal and integer) can be returned by numeric functions and operators
     */
    @Override
    protected Optional<TermType> postprocessDeducedType(Optional<TermType> optionalTermType) {
        return optionalTermType
                .map(t -> INTEGER_TYPES.contains(t.getColType()) ? TermTypes.INTEGER_TERM_TYPE : t);
    }
}
