package it.unibz.inf.ontop.model.term;

import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Term that is guaranteed to be immutable.
 *
 * In the future, every term should be immutable
 */
public interface ImmutableTerm {

    boolean isGround();

    Stream<Variable> getVariableStream();

    /**
     * Returns empty when no TermType has been inferred (missing information)
     * and no non-fatal error has been detected.
     */
    Optional<TermTypeInference> inferType() throws FatalTypingException;

    EvaluationResult evaluateEq(ImmutableTerm otherTerm);
}
