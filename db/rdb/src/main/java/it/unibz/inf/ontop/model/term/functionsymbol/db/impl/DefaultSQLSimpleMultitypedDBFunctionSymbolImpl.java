package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

public class DefaultSQLSimpleMultitypedDBFunctionSymbolImpl extends AbstractSimpleMultitypedDBFunctionSymbol {

    protected DefaultSQLSimpleMultitypedDBFunctionSymbolImpl(String nameInDialect, int arity, DBTermType targetType,
                                                             boolean isInjective) {
        super(nameInDialect, arity, targetType, isInjective);
    }
}
