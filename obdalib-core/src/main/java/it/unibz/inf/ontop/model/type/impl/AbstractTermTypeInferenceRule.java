package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.Term;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeException;
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
    public Optional<TermType> inferType(List<Term> terms, Predicate.COL_TYPE[] expectedBaseTypes) throws TermTypeException {

        if (expectedBaseTypes.length != terms.size()) {
            throw new IllegalArgumentException("Arity mismatch between " + terms + " and " + expectedBaseTypes);
        }

        ImmutableList<Optional<TermType>> argumentTypes = ImmutableList.copyOf(
                terms.stream()
                        .map(TermTypeInferenceTools::inferType)
                        .collect(Collectors.toList()));

        /**
         * Checks the argument types
         */
        IntStream.range(0, terms.size())
                .forEach(i -> argumentTypes.get(i)
                        .ifPresent(t -> {
                            Predicate.COL_TYPE expectedBaseType = expectedBaseTypes[i];
                            if ((expectedBaseType != null) && (!t.isCompatibleWith(expectedBaseType))) {
                                throw new TermTypeException(terms.get(i), new TermTypeImpl(expectedBaseType), t);
                            }
                        }));
        doAdditionalChecks(terms, argumentTypes);

        return postprocessDeducedType(deduceType(argumentTypes));
    }

    /**
     * Hook, does nothing by default
     */
    protected Optional<TermType> postprocessDeducedType(Optional<TermType> termType) {
        return termType;
    }

    /**
     * Hook, does nothing by default
     */
    protected void doAdditionalChecks(List<Term> terms, ImmutableList<Optional<TermType>> argumentTypes)
            throws TermTypeException {
    }

    /**
     * TODO: find a better name
     */
    protected abstract Optional<TermType> deduceType(ImmutableList<Optional<TermType>> argumentTypes);

}
