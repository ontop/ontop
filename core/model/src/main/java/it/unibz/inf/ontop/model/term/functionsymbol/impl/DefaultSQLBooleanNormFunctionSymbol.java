package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

/**
 * SQL-specific
 */
public class DefaultSQLBooleanNormFunctionSymbol extends AbstractBooleanNormFunctionSymbol {

    protected DefaultSQLBooleanNormFunctionSymbol(DBTermType booleanType, DBTermType stringType) {
        super(booleanType, stringType);
    }
}
