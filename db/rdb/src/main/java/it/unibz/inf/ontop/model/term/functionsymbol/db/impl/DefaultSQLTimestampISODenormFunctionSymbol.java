package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

public class DefaultSQLTimestampISODenormFunctionSymbol extends AbstractTimestampISODenormFunctionSymbol {

    protected DefaultSQLTimestampISODenormFunctionSymbol(DBTermType timestampType, DBTermType dbStringType) {
        super(timestampType, dbStringType);
    }
}
