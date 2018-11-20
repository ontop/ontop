package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

public class DefaultSQLTimestampISONormFunctionSymbol extends AbstractTimestampISONormFunctionSymbol {

    protected DefaultSQLTimestampISONormFunctionSymbol(DBTermType timestampType, DBTermType dbStringType) {
        super(timestampType, dbStringType);
    }
}
