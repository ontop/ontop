package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.R2RMLIRISafeEncoder;

import java.util.Map;
import java.util.function.Function;

public class DefaultSQLR2RMLSafeIRIEncodeFunctionSymbol extends AbstractTypedDBFunctionSymbol {

    private final String ENCODE_FOR_URI_START, ENCODE_FOR_URI_END;

    protected DefaultSQLR2RMLSafeIRIEncodeFunctionSymbol(DBTermType dbStringType) {
        super("R2RMLIRISafeEncode", ImmutableList.of(dbStringType), dbStringType);
        /*
         * Imported from SQL99DialectAdapter
         */
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (Map.Entry<String, String> e : R2RMLIRISafeEncoder.TABLE.entrySet()) {
            sb1.append("REPLACE(");
            String value = e.getValue();
            String encode = e.getKey();
            sb2.append(", ").append(getSQLLexicalFormString(value))
                    .append(", ").append(getSQLLexicalFormString(encode))
                    .append(")");

        }
        ENCODE_FOR_URI_START = sb1.toString();
        ENCODE_FOR_URI_END = sb2.toString();
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    /**
     * Imported from SQL99DialectAdapter
     */
    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        return ENCODE_FOR_URI_START + termConverter.apply(terms.get(0)) + ENCODE_FOR_URI_END;
    }

    /**
     * Imported from SQL99DialectAdapter
     */
    protected String getSQLLexicalFormString(String constant) {
        return "'" + constant.replaceAll("(?<!')'(?!')", escapedSingleQuote()) + "'";
    }

    /**
     * Imported from SQL99DialectAdapter
     */
    protected String escapedSingleQuote() {
        return "''";
    }
}
