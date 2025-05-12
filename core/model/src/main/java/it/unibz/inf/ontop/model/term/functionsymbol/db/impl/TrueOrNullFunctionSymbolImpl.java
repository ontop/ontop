package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.TrueOrNullFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.Function;

public class TrueOrNullFunctionSymbolImpl extends AbstractOrNullFunctionSymbol implements TrueOrNullFunctionSymbol {
    private final DBTermType dbBooleanType;

    protected TrueOrNullFunctionSymbolImpl(int arity, DBTermType dbBooleanTermType) {
        super("TRUE_OR_NULL" + arity, arity, dbBooleanTermType, true);
        this.dbBooleanType = dbBooleanTermType;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {

        ImmutableExpression condition = termFactory.getDisjunction((ImmutableList<ImmutableExpression>) terms);

        ImmutableFunctionalTerm newExpression = termFactory.getBooleanIfElseNull(condition, termFactory.getIsTrue(termFactory.getDBBooleanConstant(true)));
        return termConverter.apply(newExpression);
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        DBConstant trueConstant = termFactory.getDBBooleanConstant(true);
        if (newTerms.stream()
                .anyMatch(trueConstant::equals))
            return trueConstant;

        /*
         * We don't care about other constants
         */
        ImmutableList<ImmutableExpression> remainingExpressions = newTerms.stream()
                .filter(t -> (t instanceof ImmutableExpression))
                .map(t -> (ImmutableExpression) t)
                .collect(ImmutableCollectors.toList());

        int newArity = remainingExpressions.size();
        return remainingExpressions.isEmpty()
                ? termFactory.getNullConstant()
                : termFactory.getImmutableExpression(
                        new TrueOrNullFunctionSymbolImpl(newArity, dbBooleanType),
                remainingExpressions);
    }
}
