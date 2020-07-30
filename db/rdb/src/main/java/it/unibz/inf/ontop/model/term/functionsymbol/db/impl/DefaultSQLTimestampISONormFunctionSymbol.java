package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

public class DefaultSQLTimestampISONormFunctionSymbol extends AbstractTimestampISONormFunctionSymbol {

    protected DefaultSQLTimestampISONormFunctionSymbol(DBTermType timestampType, DBTermType dbStringType,
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
