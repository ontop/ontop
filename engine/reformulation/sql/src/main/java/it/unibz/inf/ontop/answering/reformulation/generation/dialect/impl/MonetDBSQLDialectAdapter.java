package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

/**
 * Created by elem on 22/06/15.
 */
public class MonetDBSQLDialectAdapter extends SQL99DialectAdapter {



    @Override
    public String getSQLLexicalFormString(String constant) {
        return "'" + constant + "'";
    }


}
