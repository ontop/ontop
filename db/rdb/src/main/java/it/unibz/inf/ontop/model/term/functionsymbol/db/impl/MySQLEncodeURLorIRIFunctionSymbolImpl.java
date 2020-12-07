package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.regex.Pattern;

public class MySQLEncodeURLorIRIFunctionSymbolImpl extends DefaultSQLEncodeURLorIRIFunctionSymbol {

    protected MySQLEncodeURLorIRIFunctionSymbolImpl(DBTermType dbStringType, boolean preserveInternationalChars) {
        super(dbStringType, preserveInternationalChars);
    }

    private static final Pattern BACKSLASH = Pattern.compile("\\\\");

    @Override
    protected String encodeSQLStringConstant(String constant) {
        return super.encodeSQLStringConstant(BACKSLASH.matcher(constant).replaceAll("\\\\\\\\"));
    }
}
