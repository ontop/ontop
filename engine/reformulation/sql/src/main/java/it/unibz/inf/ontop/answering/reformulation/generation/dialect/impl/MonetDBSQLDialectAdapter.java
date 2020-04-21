package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by elem on 22/06/15.
 */
public class MonetDBSQLDialectAdapter extends SQL99DialectAdapter {



    @Override
    public String getSQLLexicalFormString(String constant) {
        return "'" + constant + "'";
    }


    @Override
    public String nameTopVariable(String variableName, Set<String> sqlVariableNames) {
        return variableName;
    }

}
