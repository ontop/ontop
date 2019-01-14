package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class DefaultSQLIsStringEmptyFunctionSymbol extends AbstractDBIsStringEmptyFunctionSymbol {

    private static final String TEMPLATE = "(%s = 0)";


    protected DefaultSQLIsStringEmptyFunctionSymbol(DBTermType dbBooleanType, DBTermType abstractRootDBType) {
        super(dbBooleanType, abstractRootDBType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(TEMPLATE, termConverter.apply(termFactory.getDBCharLength(terms.get(0))));
    }
}
