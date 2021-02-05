package it.unibz.inf.ontop.model.term;


import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;

import java.util.Optional;
import java.util.stream.Stream;


public interface ImmutableExpression extends ImmutableFunctionalTerm {

    @Override
    BooleanFunctionSymbol getFunctionSymbol();

    /**
     * Flattens AND expressions.
     */
    Stream<ImmutableExpression> flattenAND();

    /**
     * Flattens OR expressions.
     */
    Stream<ImmutableExpression> flattenOR();

    Evaluation evaluate(VariableNullability variableNullability);

    IncrementalEvaluation evaluate(VariableNullability variableNullability, boolean isExpressionNew);

    /**
     * 2-valued logic (2VL): NULL is reduced to FALSE
     * Is intended to be used by filtering condition, where both NULL and FALSE cause the condition to be rejected.
     *
     */
    Evaluation evaluate2VL(VariableNullability variableNullability);

    IncrementalEvaluation evaluate2VL(VariableNullability variableNullability, boolean isExpressionNew);

    ImmutableTerm simplify2VL(VariableNullability variableNullability);

    boolean isVar2VarEquality();

    /**
     * Returns the equivalent to NOT(this)
     */
    ImmutableExpression negate(TermFactory termFactory);

    interface Evaluation {

        Optional<ImmutableExpression> getExpression();
        Optional<BooleanValue> getValue();
        ImmutableTerm getTerm();

        IncrementalEvaluation getEvaluationResult(ImmutableExpression originalExpression,
                                                  boolean wasExpressionAlreadyNew);

        enum BooleanValue {
            TRUE,
            FALSE,
            NULL
        }

        default boolean isEffectiveFalse() {
            return getValue()
                    .filter(v -> {
                        switch (v) {
                            case FALSE:
                            case NULL:
                                return true;
                            default:
                                return false;
                        }
                    })
                    .isPresent();
        }
    }
}
