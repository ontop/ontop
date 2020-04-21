package it.unibz.inf.ontop.answering.reformulation.generation.dialect;

import it.unibz.inf.ontop.model.term.DBConstant;

import java.util.Set;

public interface SQLDialectAdapter {

	/**
	 * Allows the SQL dialect adapter to put restrict on the name (e.g. name length).
	 */
	String nameTopVariable(String signatureVariable, Set<String> sqlVariableNames);


    String render(DBConstant constant);

	String getTopNSQL(String sqlQuery, int top);
}
