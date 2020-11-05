package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

public class MySQLEncodeURLorIRIFunctionSymbolImpl extends DefaultSQLEncodeURLorIRIFunctionSymbol {

    protected MySQLEncodeURLorIRIFunctionSymbolImpl(DBTermType dbStringType, boolean preserveInternationalChars) {
        super(dbStringType, preserveInternationalChars);
    }

    @Override
    protected String encodeSQLStringConstant(String constant) {
        return super.encodeSQLStringConstant(constant.replace("\\", "\\\\"));
    }
}
