package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;


public class DBStrAfterFunctionSymbolImpl extends AbstractDBStrBeforeOrAfterFunctionSymbol {

    protected DBStrAfterFunctionSymbolImpl(DBTermType dbStringType, DBTermType rootDBType,
                                           DBFunctionSymbolSerializer serializer) {
        super("DB_STRAFTER", dbStringType, rootDBType, serializer);
    }

    /**
     * Could be implemented
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
