package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;

/**
 * SQL-specific
 */
public class DefaultSQLSimpleDBCastFunctionSymbol extends AbstractSimpleDBCastFunctionSymbol {

    protected DefaultSQLSimpleDBCastFunctionSymbol(@Nonnull DBTermType inputBaseType, DBTermType targetType) {
        super(inputBaseType, targetType);
    }

    @Override
    public boolean canBePostProcessed() {
        return getInputType().isPresent();
    }
}
