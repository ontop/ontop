package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

public class DefaultSQLSimpleTypedDBFunctionSymbol extends SimpleTypedDBFunctionSymbolImpl {


    protected DefaultSQLSimpleTypedDBFunctionSymbol(String nameInDialect, int arity, DBTermType targetType,
                                                    boolean isInjective, DBTermType rootDBTermType) {
        super(nameInDialect, arity, targetType, isInjective, rootDBTermType,
                Serializers.getRegularSerializer(nameInDialect));
    }
}
