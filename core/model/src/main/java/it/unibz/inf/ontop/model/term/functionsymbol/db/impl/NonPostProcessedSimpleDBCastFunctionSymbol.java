package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;

/**
 * Forbids post-processing.
 *
 * For instance, useful when the JDBC driver converts in a strange manner the JDBC results
 */
public class NonPostProcessedSimpleDBCastFunctionSymbol extends DefaultSimpleDBCastFunctionSymbol {

    protected NonPostProcessedSimpleDBCastFunctionSymbol(@Nonnull DBTermType inputBaseType, DBTermType targetType,
                                                         DBFunctionSymbolSerializer serializer) {
        super(inputBaseType, targetType, serializer);
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
