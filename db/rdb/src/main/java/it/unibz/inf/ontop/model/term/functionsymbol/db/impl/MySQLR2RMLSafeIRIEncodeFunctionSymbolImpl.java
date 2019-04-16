package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

public class MySQLR2RMLSafeIRIEncodeFunctionSymbolImpl extends DefaultSQLR2RMLSafeIRIEncodeFunctionSymbol {

    protected MySQLR2RMLSafeIRIEncodeFunctionSymbolImpl(DBTermType dbStringType) {
        super(dbStringType);
    }

    @Override
    protected String encodeSQLStringConstant(String constant) {
        return super.encodeSQLStringConstant(constant.replace("\\", "\\\\"));
    }
}
