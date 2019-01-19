package it.unibz.inf.ontop.model.term;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Term that is guaranteed to be immutable.
 *
 * In the future, every term should be immutable
 */
public interface ImmutableTerm {

    /**
     * Returns true if and only if the term is a NULL Constant.
     */
    boolean isNull();

    boolean isGround();

    Stream<Variable> getVariableStream();

    /**
     * Returns empty when no TermType has been inferred (missing information or fatal error)
     * and no non-fatal error has been detected.
     */
    Optional<TermTypeInference> inferType();

    IncrementalEvaluation evaluateStrictEq(ImmutableTerm otherTerm, VariableNullability variableNullability);

    IncrementalEvaluation evaluateIsNotNull(VariableNullability variableNullability);

    ImmutableTerm simplify(VariableNullability variableNullability);

    /**
     * When no variableNullability is available
     */
    ImmutableTerm simplify();

    boolean isNullable(ImmutableSet<Variable> nullableVariables);
}
