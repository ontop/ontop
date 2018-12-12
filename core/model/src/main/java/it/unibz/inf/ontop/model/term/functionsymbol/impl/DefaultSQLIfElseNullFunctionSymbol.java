package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.DBTermType;

public class DefaultSQLIfElseNullFunctionSymbol extends AbstractDBIfElseNullFunctionSymbol {

    private static final String TEMPLATE = "CASE WHEN %s THEN %s ELSE NULL END";

    protected DefaultSQLIfElseNullFunctionSymbol(DBTermType dbBooleanType, DBTermType rootDBTermType) {
        super(dbBooleanType, rootDBTermType);
    }

    @Override
    public String getNativeDBString(ImmutableList<String> termStrings) {
        return String.format(TEMPLATE, termStrings.toArray());
    }
}
