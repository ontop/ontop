package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.R2RMLIRISafeEncoder;

import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DefaultSQLEncodeURLorIRIFunctionSymbol extends AbstractEncodeURIorIRIFunctionSymbol {

    private final String encodeForIriStart, encodeForIriEnd;

    protected DefaultSQLEncodeURLorIRIFunctionSymbol(DBTermType dbStringType, boolean preserveInternationalChars) {
        super(dbStringType, preserveInternationalChars);
        /*
         * Imported from SQL99DialectAdapter
         */
        this.encodeForIriStart = R2RMLIRISafeEncoder.TABLE.entrySet().stream()
                .map(e -> "REPLACE(")
                .collect(Collectors.joining());

        this.encodeForIriEnd = R2RMLIRISafeEncoder.TABLE.entrySet().stream()
                .map(e -> ", " + encodeSQLStringConstant(e.getValue().toString())
                        + ", " + encodeSQLStringConstant(e.getKey()) + ")")
                .collect(Collectors.joining());
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

    private static final Pattern QUOTATION_MARK = Pattern.compile("(?<!')'(?!')");

    /**
     * Imported from SQL99DialectAdapter
     *
     * By default, quotes and escapes isolated single quotes
     */
    protected String encodeSQLStringConstant(String constant) {
        return "'" + QUOTATION_MARK.matcher(constant).replaceAll(getEscapedSingleQuote()) + "'";
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
