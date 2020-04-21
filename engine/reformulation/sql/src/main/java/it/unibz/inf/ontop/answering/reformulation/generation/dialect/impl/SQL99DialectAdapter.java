package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.Set;

public class SQL99DialectAdapter implements SQLDialectAdapter {

    protected String escapedSingleQuote() {
        return "''";
    }


    /**
     * There is no standard for this part.
     * <p>
     * Arbitrary default implementation proposed
     * (may not work with many DB engines).
     */
    @Override
    public String sqlLimitOffset(long limit, long offset) {
        return String.format("LIMIT %d, %d", offset, limit);
    }

    @Override
    public String sqlLimit(long limit) {
        return String.format("LIMIT %d", limit);
    }

    @Override
    public String sqlOffset(long offset) {
        return String.format("OFFSET %d", offset);
    }

    @Override
    public String getTopNSQL(String sqlString, int top) {
        return String.format("%s LIMIT %d", sqlString, top);
    }

    @Override
    public String nameTopVariable(String signatureVariableName, Set<String> sqlVariableNames) {
        return buildDefaultName("", signatureVariableName, "");
    }

    @Override
    public String render(DBConstant constant) {
        DBTermType dbType = constant.getType();

        switch (dbType.getCategory()) {
            case INTEGER:
            case DECIMAL:
            case FLOAT_DOUBLE:
                // TODO: handle the special case of not-a-number!
            case BOOLEAN:
                return constant.getValue();
            default:
                return getSQLLexicalFormString(constant.getValue());
        }
    }

    /**
     * Concatenates the strings.
     * Default way to name a variable or a view.
     * <p>
     * Returns an UNQUOTED string.
     */
    protected final String buildDefaultName(String prefix, String intermediateName, String suffix) {
        return prefix + intermediateName + suffix;
    }

    /**
     * By default, quotes and escapes isolated single quotes
     */
    protected String getSQLLexicalFormString(String constant) {

        return "'" + constant.replaceAll("(?<!')'(?!')", escapedSingleQuote()) + "'";
    }
}
