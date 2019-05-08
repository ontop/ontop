package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;


public class DefaultImplicitDBCastFunctionSymbol extends DefaultSimpleDBCastFunctionSymbol {

    protected DefaultImplicitDBCastFunctionSymbol(@Nonnull DBTermType inputBaseType, DBTermType targetType) {
        super(inputBaseType, targetType, (terms, termConverter, termFactory) -> termConverter.apply(terms.get(0)));
    }
}
