package it.unibz.inf.ontop.model.term;

import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.type.TypeInference;

import java.util.stream.Stream;

/**
 * Term that is guaranteed to be immutable.
 *
 * In the future, every term should be immutable
 */
public interface ImmutableTerm {

    boolean isGround();

    Stream<Variable> getVariableStream();

    TypeInference inferType() throws FatalTypingException;

    EvaluationResult evaluateEq(ImmutableTerm otherTerm);
}
