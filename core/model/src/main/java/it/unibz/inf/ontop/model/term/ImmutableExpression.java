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
    }
}
