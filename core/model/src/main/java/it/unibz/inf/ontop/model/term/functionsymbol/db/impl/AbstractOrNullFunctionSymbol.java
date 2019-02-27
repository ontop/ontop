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
 *
 *   * NULL AND y AND ... --> FalseOrNull
 *   * NULL OR y OR ... ---> TrueOrNull
 *
 *   N-ary (at least 1)
 */
public abstract class AbstractOrNullFunctionSymbol extends DBBooleanFunctionSymbolImpl {

    private final boolean possibleBoolean;

    protected AbstractOrNullFunctionSymbol(@Nonnull String name, int arity, DBTermType dbBooleanTermType,
                                           boolean possibleBoolean) {
        super(name, IntStream.range(0, arity)
                .boxed()
                .map(i -> dbBooleanTermType)
                .collect(ImmutableCollectors.toList()), dbBooleanTermType);
        this.possibleBoolean = possibleBoolean;
        if (arity <= 0)
            throw new IllegalArgumentException("Arity must be >= 1");
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException();
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
}
