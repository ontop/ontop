package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

/**
 * For SQL Server and Oracle, which are not accepting IS_NOT_NULL over expressions
 *
 * TODO: find a better name
 */
public class ExpressionSensitiveSQLDBIsNullOrNotFunctionSymbolImpl extends DefaultSQLDBIsNullOrNotFunctionSymbol {

    protected ExpressionSensitiveSQLDBIsNullOrNotFunctionSymbolImpl(boolean isNull,
                                                                    DBTermType dbBooleanTermType, DBTermType rootDBTermType) {
        super(isNull, dbBooleanTermType, rootDBTermType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        ImmutableTerm initialTerm = terms.get(0);
        ImmutableList<? extends ImmutableTerm> newTerms = (initialTerm instanceof ImmutableExpression)
                ? ImmutableList.of(transformExpression((ImmutableExpression) initialTerm, termFactory))
                : terms;

        return super.getNativeDBString(newTerms, termConverter, termFactory);
    }

    private ImmutableFunctionalTerm transformExpression(ImmutableExpression expression, TermFactory termFactory) {
        boolean isNull = isTrueWhenNull();

        return termFactory.getIfThenElse(
                // NB: mind the 3-valued logic (3VL)!
                termFactory.getDisjunction(expression, termFactory.getDBNot(expression)),
                termFactory.getDBBooleanConstant(!isNull),
                termFactory.getDBBooleanConstant(isNull)
        );
    }
}
