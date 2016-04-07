package it.unibz.inf.ontop.model.type.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.Term;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeException;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;


public class NumericTermTypeInferenceRule extends UnifierTermTypeInferenceRule {

    /**
     * Checks that all the terms are numeric
     */
    protected void doAdditionalChecks(List<Term> terms, ImmutableList<Optional<TermType>> argumentTypes)
            throws TermTypeException {
        IntStream.range(0, terms.size())
                .forEach(i ->  {
                    if(!argumentTypes.get(i)
                            .map(TermType::isNumeric)
                            .orElse(true)) {
                        // TODO: refactor the exception
                        throw new TermTypeException(terms.get(i), null, argumentTypes.get(i).get());
                    }
                });
    }
}
