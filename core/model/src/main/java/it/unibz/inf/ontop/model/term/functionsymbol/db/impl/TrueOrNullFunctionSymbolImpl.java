package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.TrueOrNullFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class TrueOrNullFunctionSymbolImpl extends AbstractOrNullFunctionSymbol implements TrueOrNullFunctionSymbol {

    protected TrueOrNullFunctionSymbolImpl(int arity, DBTermType dbBooleanTermType) {
        super("TRUE_OR_NULL" + arity, arity, dbBooleanTermType, true);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {

        ImmutableExpression condition = termFactory.getDisjunction((ImmutableList<ImmutableExpression>) terms);

        ImmutableFunctionalTerm newExpression = termFactory.getBooleanIfElseNull(condition, termFactory.getIsTrue(termFactory.getDBBooleanConstant(true)));
        return termConverter.apply(newExpression);
    }
}
