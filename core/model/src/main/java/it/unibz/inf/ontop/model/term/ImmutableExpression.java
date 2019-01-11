package it.unibz.inf.ontop.model.term;


import com.google.common.collect.ImmutableSet;
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

    /**
     * Generalization of flattening (AND, OR, etc.).
     *
     * It is the responsibility of the caller to make sure such a flattening makes sense.
     */
    ImmutableSet<ImmutableExpression> flatten(BooleanFunctionSymbol operator);

    Evaluation evaluate(TermFactory termFactory, VariableNullability variableNullability);

    boolean isVar2VarEquality();

    /**
     * Returns the equivalent to NOT(this)
     */
    ImmutableExpression negate(TermFactory termFactory);

    interface Evaluation {

        Optional<ImmutableExpression> getExpression();
        Optional<BooleanValue> getValue();
        ImmutableTerm getTerm();

        EvaluationResult getEvaluationResult(ImmutableExpression originalExpression);

        enum BooleanValue {
            TRUE,
            FALSE,
            NULL
        }
    }
}
