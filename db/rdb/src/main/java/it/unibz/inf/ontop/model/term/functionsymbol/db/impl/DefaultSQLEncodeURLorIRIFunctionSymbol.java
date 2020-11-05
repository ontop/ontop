package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.R2RMLIRISafeEncoder;

import java.util.Map;
import java.util.function.Function;

public class DefaultSQLEncodeURLorIRIFunctionSymbol extends AbstractEncodeURIorIRIFunctionSymbol {

    private final String encodeForIriStart, encodeForIriEnd;

    protected DefaultSQLEncodeURLorIRIFunctionSymbol(DBTermType dbStringType, boolean preserveInternationalChars) {
        super(dbStringType, preserveInternationalChars);
        /*
         * Imported from SQL99DialectAdapter
         */
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (Map.Entry<String, String> e : R2RMLIRISafeEncoder.TABLE.entrySet()) {
            sb1.append("REPLACE(");
            String value = e.getValue();
            String encode = e.getKey();
            sb2.append(", ").append(encodeSQLStringConstant(value))
                    .append(", ").append(encodeSQLStringConstant(encode))
                    .append(")");

        }
        this.encodeForIriStart = sb1.toString();
        this.encodeForIriEnd = sb2.toString();
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    /**
     * Derived from SQL99DialectAdapter
     */
    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        return encodeForIriStart + termConverter.apply(terms.get(0)) + encodeForIriEnd;
    }

    /**
     * Imported from SQL99DialectAdapter
     *
     * By default, quotes and escapes isolated single quotes
     */
    protected String encodeSQLStringConstant(String constant) {
        return "'" + constant.replaceAll("(?<!')'(?!')", getEscapedSingleQuote()) + "'";
    }

    /**
     * Imported from SQL99DialectAdapter
     *
     * By default, escapes single quotes by doubling them
     *
     */
    protected String getEscapedSingleQuote() {
        return "''";
    }
}
