package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class SQLServerDBIsTrueFunctionSymbolImpl extends DefaultDBIsTrueFunctionSymbol {

    protected SQLServerDBIsTrueFunctionSymbolImpl(DBTermType dbBooleanTermType) {
        super(dbBooleanTermType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        ImmutableExpression newTerm = termFactory.getStrictEquality(terms.get(0), termFactory.getDBBooleanConstant(true));
        return inBrackets(termConverter.apply(newTerm));
    }
}
