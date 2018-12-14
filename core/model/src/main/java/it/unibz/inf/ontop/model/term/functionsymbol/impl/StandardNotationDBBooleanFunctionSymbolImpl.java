package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;

public abstract class StandardNotationDBBooleanFunctionSymbolImpl extends DBBooleanFunctionSymbolImpl {

    private final String nameInDialect;
    private static final String FUNCTIONAL_TEMPLATE = "%s(%s)";

    protected StandardNotationDBBooleanFunctionSymbolImpl(String nameInDialect, ImmutableList<TermType> expectedBaseTypes,
                                                          DBTermType dbBooleanTermType) {
        super(nameInDialect + expectedBaseTypes.size(), expectedBaseTypes, dbBooleanTermType);
        this.nameInDialect = nameInDialect;
    }

    @Override
    public String getNativeDBString(ImmutableList<String> termStrings) {
        return String.format(FUNCTIONAL_TEMPLATE, nameInDialect,
                String.join( ",", termStrings));
    }
}
