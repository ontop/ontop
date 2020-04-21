package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Set;

public class SQL99DialectAdapter implements SQLDialectAdapter {

    protected String escapedSingleQuote() {
        return "''";
    }



    @Override
    public String getTopNSQL(String sqlString, int top) {
        return String.format("%s LIMIT %d", sqlString, top);
    }

    @Override
    public String nameTopVariable(String signatureVariableName, Set<String> sqlVariableNames) {
        return signatureVariableName;
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
     * By default, quotes and escapes isolated single quotes
     */
    protected String getSQLLexicalFormString(String constant) {

        return "'" + constant.replaceAll("(?<!')'(?!')", escapedSingleQuote()) + "'";
    }
}
