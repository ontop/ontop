package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by elem on 22/06/15.
 */
public class MonetDBSQLDialectAdapter extends SQL99DialectAdapter {

    @Override
    public String sqlLimitOffset(long limit, long offset) {
        return String.format("LIMIT %d OFFSET %d", offset, limit);
    }

    @Override
    public String sqlLimit(long limit) {
        return String.format("LIMIT %d", limit);
    }

    @Override
    public String sqlOffset(long offset) {
        return String.format("OFFSET %d", offset);
    }


    // default
    // public String getTopNSQL(String sqlString, int top) {
    //    String slice = String.format("LIMIT %d", top);
    //    return String.format("%s %s", sqlString, slice);
    // }


    @Override
    public String getSQLLexicalFormString(String constant) {
        return "'" + constant + "'";
    }


    @Override
    public String nameTopVariable(String variableName, Set<String> sqlVariableNames) {
        return variableName;
    }

}
