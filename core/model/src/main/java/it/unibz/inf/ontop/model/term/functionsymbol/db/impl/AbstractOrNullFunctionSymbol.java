package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;


/**
 * Implementing the ternary logic for situations like:
 *{@code
 *   * NULL AND y AND ... --> FalseOrNull
 *   * NULL OR y OR ... ---> TrueOrNull
 *}
 *   N-ary (at least 1)
 */
public abstract class AbstractOrNullFunctionSymbol extends DBBooleanFunctionSymbolImpl {

    private final boolean possibleBoolean;

    protected AbstractOrNullFunctionSymbol(@Nonnull String name, int arity, DBTermType dbBooleanTermType,
                                           boolean possibleBoolean) {
        super(name, IntStream.range(0, arity)
                .mapToObj(i -> dbBooleanTermType)
                .collect(ImmutableCollectors.toList()), dbBooleanTermType);
        this.possibleBoolean = possibleBoolean;
        if (arity <= 0)
            throw new IllegalArgumentException("Arity must be >= 1");
    }

    @Override
    public boolean blocksNegation() {
        return false;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        ImmutableList<ImmutableExpression> negatedSubTerms = subTerms.stream()
                .map(t -> (ImmutableExpression) t)
                .map(e -> e.negate(termFactory))
                .collect(ImmutableCollectors.toList());
        return possibleBoolean
                ? termFactory.getFalseOrNullFunctionalTerm(negatedSubTerms)
                : termFactory.getTrueOrNullFunctionalTerm(negatedSubTerms);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        DBConstant possibleBooleanConstant = termFactory.getDBBooleanConstant(possibleBoolean);
        if (newTerms.stream()
                .anyMatch(possibleBooleanConstant::equals))
            return possibleBooleanConstant;

        /*
         * We don't care about other constants
         */
        ImmutableList<ImmutableExpression> remainingExpressions = newTerms.stream()
                .filter(t -> (t instanceof ImmutableExpression))
                .map(t -> (ImmutableExpression) t)
                .collect(ImmutableCollectors.toList());

        return remainingExpressions.isEmpty()
                ? termFactory.getNullConstant()
                : termFactory.getImmutableExpression(this, remainingExpressions);
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return true;
    }

    /**
     * 2VL: the NULL is considered "equivalent" to FALSE
     */
    @Override
    public ImmutableTerm simplify2VL(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                                     VariableNullability variableNullability) {
        // TRUE or NULL
        if (possibleBoolean) {
            return termFactory.getDisjunction((ImmutableList<ImmutableExpression>)terms)
                    .simplify2VL(variableNullability);
        }
        // FALSE or NULL
        else
            return termFactory.getDBBooleanConstant(false);
    }

    /**
     * Requires its arguments to be expressions
     */
    @Override
    protected ImmutableList<? extends ImmutableTerm> transformIntoRegularArguments(
            ImmutableList<? extends NonFunctionalTerm> arguments, TermFactory termFactory) {
        return arguments.stream()
                .map(termFactory::getIsTrue)
                .collect(ImmutableCollectors.toList());
    }
}
