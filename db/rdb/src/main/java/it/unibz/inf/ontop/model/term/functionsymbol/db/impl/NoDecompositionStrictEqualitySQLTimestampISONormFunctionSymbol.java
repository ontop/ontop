package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

/**
 * TODO: find a better name
 */
public class NoDecompositionStrictEqualitySQLTimestampISONormFunctionSymbol extends AbstractTimestampISONormFunctionSymbol {

    protected NoDecompositionStrictEqualitySQLTimestampISONormFunctionSymbol(DBTermType timestampType, DBTermType dbStringType,
                                                                             DBFunctionSymbolSerializer serializer) {
        super(timestampType, dbStringType, serializer);
    }

    /**
     * TODO: try to return a constant
     */
    @Override
    protected ImmutableTerm convertDBConstant(DBConstant constant, TermFactory termFactory) {
        return termFactory.getImmutableFunctionalTerm(this, constant);
    }
}
