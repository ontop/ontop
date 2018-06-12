package it.unibz.inf.ontop.model.type;

import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceImpl;

import java.util.Optional;

/**
 * Can have two states: (i) determined or (ii) non fatal error.
 */
public interface TermTypeInference {

    /**
     * Only present when the status is determined
     */
    Optional<TermType> getTermType();

    boolean isNonFatalError();

    static TermTypeInference declareTermType(TermType termType) {
        return TermTypeInferenceImpl.declareTermType(termType);
    }

    static TermTypeInference declareNonFatalError() {
        return TermTypeInferenceImpl.declareNonFatalError();
    }
}
