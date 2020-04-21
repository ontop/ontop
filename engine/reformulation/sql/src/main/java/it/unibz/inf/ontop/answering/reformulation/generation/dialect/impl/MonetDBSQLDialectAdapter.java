package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by elem on 22/06/15.
 */
public class MonetDBSQLDialectAdapter extends SQL99DialectAdapter {

    /**
     * https://www.monetdb.org/Documentation/SQLreference/TableExpressions
     *  [ LIMIT posint ]
     *  [ OFFSET posint ]
     */

    // sqlLimit, sqlOffset, sqlTopNSQL are standard

    @Override
    public String sqlLimitOffset(long limit, long offset) {
        return String.format("LIMIT %d\nOFFSET %d", offset, limit);
    }


    @Override
    public String getSQLLexicalFormString(String constant) {
        return "'" + constant + "'";
    }


    @Override
    public String nameTopVariable(String variableName, Set<String> sqlVariableNames) {
        return variableName;
    }

}
