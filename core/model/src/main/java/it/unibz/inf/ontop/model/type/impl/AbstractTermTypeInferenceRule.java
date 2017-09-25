package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.exception.IncompatibleTermException;
import it.unibz.inf.ontop.model.type.TermTypeInferenceRule;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;

/**
 * TODO: explain
 */
public abstract class AbstractTermTypeInferenceRule implements TermTypeInferenceRule {

    @Override
    public Optional<TermType> inferType(List<Term> terms, ImmutableList<Optional<TermType>> expectedBaseTypes)
            throws IncompatibleTermException {

        ImmutableList<Optional<TermType>> argumentTypes = ImmutableList.copyOf(
                terms.stream()
                        .map(TermTypeInferenceTools::inferType)
                        .collect(Collectors.toList()));
        return inferTypeFromArgumentTypes(argumentTypes, expectedBaseTypes);

    }

    @Override
    public Optional<TermType> inferTypeFromArgumentTypes(ImmutableList<Optional<TermType>> argumentTypes,
                                                  ImmutableList<Optional<TermType>> expectedBaseTypes)
            throws IncompatibleTermException {

        /*
         * TODO: restore inequality test between the arities
         */
        if (expectedBaseTypes.size() < argumentTypes.size()) {
            throw new IllegalArgumentException("Arity mismatch between " + argumentTypes + " and " + expectedBaseTypes);
        }

        /*
         * Checks the argument types
         */
        IntStream.range(0, argumentTypes.size())
                .forEach(i -> argumentTypes.get(i)
                        .ifPresent(t -> expectedBaseTypes.get(i).ifPresent(expectedBaseType -> {
                            if (!t.isCompatibleWith(expectedBaseType)) {
                                throw new IncompatibleTermException(expectedBaseType, t);
                            }
                        })));
        doAdditionalChecks(argumentTypes);

        return postprocessInferredType(reduceInferredTypes(argumentTypes));
    }

    /**
     * Hook, does nothing by default
     */
    protected Optional<TermType> postprocessInferredType(Optional<TermType> termType) {
        return termType;
    }

    /**
     * Hook, does nothing by default
     */
    protected void doAdditionalChecks(ImmutableList<Optional<TermType>> argumentTypes)
            throws IncompatibleTermException {
    }

    /**
     * TODO: find a better name
     */
    protected abstract Optional<TermType> reduceInferredTypes(ImmutableList<Optional<TermType>> argumentTypes);

}
