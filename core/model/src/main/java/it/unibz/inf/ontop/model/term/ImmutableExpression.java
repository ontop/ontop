package it.unibz.inf.ontop.model.term;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.TermTypeInference;

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

    Evaluation evaluate(TermFactory termFactory);

    boolean isVar2VarEquality();

    /**
     * Returns the equivalent to NOT(this)
     */
    ImmutableExpression negate(TermFactory termFactory);

    interface Evaluation {

        Optional<ImmutableExpression> getExpression();
        Optional<BooleanValue> getValue();
        ImmutableTerm getTerm();

        enum BooleanValue {
            TRUE,
            FALSE,
            NULL
        }
    }
}
