package it.unibz.inf.ontop.model.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.model.Term;
import it.unibz.inf.ontop.model.TermType;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
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

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

    @Override
    public Optional<TermType> inferType(List<Term> terms, ImmutableList<Optional<Predicate.COL_TYPE>> expectedBaseTypes)
            throws TermTypeException {

        /**
         * TODO: restore inequality test between the arities
         */
        if (expectedBaseTypes.size() < terms.size()) {
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
                        .ifPresent(t -> expectedBaseTypes.get(i).ifPresent(expectedBaseType -> {
                            if (!t.isCompatibleWith(expectedBaseType)) {
                                throw new TermTypeException(terms.get(i), DATA_FACTORY.getTermType(expectedBaseType), t);
                            }
                        })));
        doAdditionalChecks(terms, argumentTypes);

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
    protected void doAdditionalChecks(List<Term> terms, ImmutableList<Optional<TermType>> argumentTypes)
            throws TermTypeException {
    }

    /**
     * TODO: find a better name
     */
    protected abstract Optional<TermType> reduceInferredTypes(ImmutableList<Optional<TermType>> argumentTypes);

}
