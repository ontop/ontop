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

        ImmutableList<Optional<TermType>> argumentTypes = ImmutableList.copyOf(
                terms.stream()
                        .map(TermTypeInferenceTools::inferType)
                        .collect(Collectors.toList()));
        return inferTypeFromArgumentTypes(argumentTypes, expectedBaseTypes);

    }

    @Override
    public Optional<TermType> inferTypeFromArgumentTypes(ImmutableList<Optional<TermType>> argumentTypes,
                                                  ImmutableList<Optional<Predicate.COL_TYPE>> expectedBaseTypes)
            throws TermTypeException {

        /**
         * TODO: restore inequality test between the arities
         */
        if (expectedBaseTypes.size() < argumentTypes.size()) {
            throw new IllegalArgumentException("Arity mismatch between " + argumentTypes + " and " + expectedBaseTypes);
        }

        /**
         * Checks the argument types
         */
        IntStream.range(0, argumentTypes.size())
                .forEach(i -> argumentTypes.get(i)
                        .ifPresent(t -> expectedBaseTypes.get(i).ifPresent(expectedBaseType -> {
                            if (!t.isCompatibleWith(expectedBaseType)) {
                                throw new TermTypeException(DATA_FACTORY.getTermType(expectedBaseType), t);
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
            throws TermTypeException {
    }

    /**
     * TODO: find a better name
     */
    protected abstract Optional<TermType> reduceInferredTypes(ImmutableList<Optional<TermType>> argumentTypes);

}
