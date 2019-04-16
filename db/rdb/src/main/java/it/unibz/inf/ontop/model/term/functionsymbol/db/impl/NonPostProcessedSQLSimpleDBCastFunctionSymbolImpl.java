package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;

/**
 * Forbids post-processing.
 *
 * Useful when the JDBC driver converts in a strange manner the JDBC results
 */
public class NonPostProcessedSQLSimpleDBCastFunctionSymbolImpl extends DefaultSQLSimpleDBCastFunctionSymbol {

    protected NonPostProcessedSQLSimpleDBCastFunctionSymbolImpl(@Nonnull DBTermType inputBaseType, DBTermType targetType) {
        super(inputBaseType, targetType);
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
