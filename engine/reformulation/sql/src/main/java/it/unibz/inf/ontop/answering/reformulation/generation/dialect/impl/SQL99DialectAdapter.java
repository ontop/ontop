package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;

public class SQL99DialectAdapter implements SQLDialectAdapter {

    @Override
    public String getTopNSQL(String sqlString, int top) {
        return String.format("%s LIMIT %d", sqlString, top);
    }

}
