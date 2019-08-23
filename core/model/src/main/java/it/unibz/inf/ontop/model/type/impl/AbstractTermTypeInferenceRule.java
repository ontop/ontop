package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.AbstractTermTypeException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.exception.IncompatibleTermException;
import it.unibz.inf.ontop.model.type.TermTypeInferenceRule;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * TODO: explain
 */
public abstract class AbstractTermTypeInferenceRule implements TermTypeInferenceRule {

    @Override
    public Optional<TermType> inferTypeFromArgumentTypes(ImmutableList<Optional<TermType>> argumentTypes)
            throws IncompatibleTermException {

        return postprocessInferredType(reduceInferredTypes(argumentTypes));
    }

    /**
     * Hook, does nothing by default
     */
    protected Optional<TermType> postprocessInferredType(Optional<TermType> termType) {
        return termType;
    }

    /**
     * TODO: find a better name
     */
    protected abstract Optional<TermType> reduceInferredTypes(ImmutableList<Optional<TermType>> argumentTypes);

}
