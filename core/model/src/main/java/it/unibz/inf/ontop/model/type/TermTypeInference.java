package it.unibz.inf.ontop.model.type;

import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceImpl;

import java.util.Optional;

/**
 * Can have two states: (i) determined or (ii) non fatal error.
 *
 * Non-fatal error: corresponds to returning a NULL value.
 * SPARQL errors are non-fatal errors.
 *
 */
public interface TermTypeInference {

    /**
     * Only present when the status is determined
     */
    Optional<TermType> getTermType();

    static TermTypeInference declareTermType(TermType termType) {
        return TermTypeInferenceImpl.declareTermType(termType);
    }
}
