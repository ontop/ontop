package it.unibz.inf.ontop.answering.reformulation.generation.dialect;

public interface SQLDialectAdapter {

	// TODO: move to GUI, where it's used

	String getTopNSQL(String sqlQuery, int top);
}
