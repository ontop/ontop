package it.unibz.inf.ontop.answering.reformulation.generation.dialect;

import it.unibz.inf.ontop.model.term.DBConstant;

import java.util.Optional;
import java.util.Set;

public interface SQLDialectAdapter {

	String sqlSlice(long limit, long offset);

	/**
	 * Returns the name of special system table having one tuple only if the FROM clause is always required in the dialect
	 */
	Optional<String> getTrueTable();


	/**
	 * Allows the SQL dialect adapter to put restrict on the name (e.g. name length).
	 */
	String nameTopVariable(String signatureVariable, Set<String> sqlVariableNames);


    String render(DBConstant constant);
}
