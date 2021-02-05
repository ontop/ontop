package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;

public class MockupSimpleDBCastFunctionSymbol extends DefaultSimpleDBCastFunctionSymbol {

    protected MockupSimpleDBCastFunctionSymbol(@Nonnull DBTermType inputBaseType, DBTermType targetType) {
        super(inputBaseType, targetType, (terms, termConverter, termFactory) -> {
            throw new UnsupportedOperationException();
        } );
    }
}
