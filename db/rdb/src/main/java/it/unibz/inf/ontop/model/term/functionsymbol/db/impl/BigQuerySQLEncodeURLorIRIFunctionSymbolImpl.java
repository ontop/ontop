package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.StringUtils;

/*
 * Backslash character is an escape symbol in SparkSQL dialect. Replace '\' --> '\\' to avoid malformed queries
 */
public class BigQuerySQLEncodeURLorIRIFunctionSymbolImpl extends DefaultSQLEncodeURLorIRIFunctionSymbol {

    protected BigQuerySQLEncodeURLorIRIFunctionSymbolImpl(DBTermType dbStringType, boolean preserveInternationalChars) {
        super(dbStringType, preserveInternationalChars);
    }

    private static final ImmutableMap<Character, String> BACKSLASH = ImmutableMap.of('\\', "\\\\");

    @Override
    protected String getEscapedSingleQuote() {
        return "\\\\'";
    }

    @Override
    protected String encodeSQLStringConstant(String constant) {
        return super.encodeSQLStringConstant(StringUtils.encode(constant, BACKSLASH));
    }
}
