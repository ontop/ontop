package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Set;

public class SQL99DialectAdapter implements SQLDialectAdapter {


    @Override
    public String getTopNSQL(String sqlString, int top) {
        return String.format("%s LIMIT %d", sqlString, top);
    }

    @Override
    public String nameTopVariable(String signatureVariableName, Set<String> sqlVariableNames) {
        return signatureVariableName;
    }


}
