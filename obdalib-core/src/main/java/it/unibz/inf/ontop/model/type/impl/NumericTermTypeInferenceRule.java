package it.unibz.inf.ontop.model.type.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.Term;
import it.unibz.inf.ontop.model.TermType;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.type.TermTypeException;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER;
import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.NUMERIC_TYPES;
import static it.unibz.inf.ontop.model.Predicate.COL_TYPE.INTEGER_TYPES;


public class NumericTermTypeInferenceRule extends UnifierTermTypeInferenceRule {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

    /**
     * Checks that all the terms are numeric
     */
    protected void doAdditionalChecks(List<Term> terms, ImmutableList<Optional<TermType>> argumentTypes)
            throws TermTypeException {
        IntStream.range(0, terms.size())
                .forEach(i ->  {
                    if(!argumentTypes.get(i)
                            .map(t -> NUMERIC_TYPES.contains(t.getColType()))
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
                .map(t -> INTEGER_TYPES.contains(t.getColType()) ? DATA_FACTORY.getTermType(INTEGER) : t);
    }
}
