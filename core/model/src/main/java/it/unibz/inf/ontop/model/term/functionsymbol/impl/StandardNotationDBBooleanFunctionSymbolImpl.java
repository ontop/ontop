package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.DBTermType;

public abstract class StandardNotationDBBooleanFunctionSymbolImpl extends DBBooleanFunctionSymbolImpl {

    private final String nameInDialect;
    private static final String FUNCTIONAL_TEMPLATE = "%s(%s)";

    protected StandardNotationDBBooleanFunctionSymbolImpl(String nameInDialect, int arity, DBTermType dbBooleanTermType) {
        super(nameInDialect, arity, dbBooleanTermType);
        this.nameInDialect = nameInDialect;
    }

    @Override
    public String getNativeDBString(ImmutableList<String> termStrings) {
        return String.format(FUNCTIONAL_TEMPLATE, nameInDialect,
                String.join( ",", termStrings));
    }
}
