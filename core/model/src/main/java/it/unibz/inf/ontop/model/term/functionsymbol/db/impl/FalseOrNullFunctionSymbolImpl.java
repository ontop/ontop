package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.FalseOrNullFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class FalseOrNullFunctionSymbolImpl extends AbstractOrNullFunctionSymbol implements FalseOrNullFunctionSymbol {


    protected FalseOrNullFunctionSymbolImpl(int arity, DBTermType dbBooleanTermType) {
        super("FALSE_OR_NULL" + arity, arity, dbBooleanTermType, false);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {

        ImmutableExpression condition = termFactory.getDisjunction(terms.stream()
                .map(t -> (ImmutableExpression) t)
                .map(t -> t.negate(termFactory))).get();

        ImmutableFunctionalTerm newExpression = termFactory.getIfElseNull(condition, termFactory.getDBBooleanConstant(false));
        return termConverter.apply(newExpression);
    }
}
