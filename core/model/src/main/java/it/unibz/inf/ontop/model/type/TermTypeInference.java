package it.unibz.inf.ontop.model.type;

import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.impl.TermTypeInferenceImpl;

import java.util.Optional;

/**
 * Can have two states: (i) determined or (ii) redirected to the type of a variable.
 *
 */
public interface TermTypeInference {

    /**
     * Only present when the status is determined
     */
    Optional<TermType> getTermType();

    /**
     * Only when the type cannot be determined locally
     * but corresponds to the type of a variable (defined in the sub-tree)
     */
    Optional<Variable> getRedirectionVariable();

    static TermTypeInference declareTermType(TermType termType) {
        return TermTypeInferenceImpl.declareTermType(termType);
    }

    static TermTypeInference declareRedirectionToVariable(Variable variable) {
        return TermTypeInferenceImpl.declareRedirectionVariable(variable);
    }
}
