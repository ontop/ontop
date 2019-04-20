package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

public class WithoutParenthesesSimpleTypedDBFunctionSymbolImpl extends SimpleTypedDBFunctionSymbolImpl {

    protected WithoutParenthesesSimpleTypedDBFunctionSymbolImpl(String nameInDialect, DBTermType targetType,
                                                                DBTermType rootDBTermType) {
        super(nameInDialect, 0, targetType, true, rootDBTermType,
                (terms, termConverter, termFactory) -> nameInDialect);
    }
}
