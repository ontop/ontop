package it.unibz.inf.ontop.model.type.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.Term;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeError;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;


public class NumericTermTypeReasoner extends UnifierTermTypeReasoner {

    /**
     * Checks that all the terms are numeric
     */
    protected void doAdditionalChecks(List<Term> terms, ImmutableList<Optional<TermType>> argumentTypes)
            throws TermTypeError {
        IntStream.range(0, terms.size())
                .forEach(i ->  {
                    if(!argumentTypes.get(i)
                            .map(TermType::isNumeric)
                            .orElse(false)) {
                        // TODO: refactor the exception
                        throw new TermTypeError(terms.get(i), null, argumentTypes.get(i).get());
                    }
                });
    }
}
