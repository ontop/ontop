package it.unibz.inf.ontop.model.type;

import it.unibz.inf.ontop.model.type.impl.TypeInferenceImpl;

import java.util.Optional;

public interface TypeInference {

    /**
     * Only present when the status is DETERMINED
     */
    Optional<TermType> getTermType();

    Status getStatus();

    enum Status {
        /*
         * Temporarily not determined or NULL
         */
        NOT_DETERMINED,
        DETERMINED,
        NON_FATAL_ERROR
    }

    static TypeInference declareTermType(TermType termType) {
        return TypeInferenceImpl.declareTermType(termType);
    }

    static TypeInference declareNotDetermined() {
        return TypeInferenceImpl.declareNotYetDetermined();
    }

    static TypeInference declareNonFatalError() {
        return TypeInferenceImpl.declareNonFatalError();
    }
}
