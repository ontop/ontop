package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by elem on 22/06/15.
 */
public class MonetDBSQLDialectAdapter extends SQL99DialectAdapter {

    @Override
    public String sqlQuote(String name) {
        //TODO: This should depend on quotes in the sql in the mappings
        return String.format("\"%s\"", name);
//		return name;
    }

    /**
     * There is no standard for this part.
     *
     * Arbitrary default implementation proposed
     * (may not work with many DB engines).
     */
    @Override
    public String sqlSlice(long limit, long offset) {
        if ((limit < 0) && (offset < 0)) {
            return "";
        }
        else if ((limit >= 0) && (offset >= 0)) {
            return String.format("LIMIT %d OFFSET %d", offset, limit);
        }
        else if (offset < 0) {
            return String.format("LIMIT %d", limit);
        }
        // Else -> (limit < 0)
        else {
            return String.format("OFFSET %d", offset);
        }
    }

    @Override
    public String getSQLLexicalFormString(String constant) {
        return "'" + constant + "'";
    }


    @Override
    public String nameTopVariable(String variableName, Set<String> sqlVariableNames) {
        return sqlQuote(variableName);
    }

}
