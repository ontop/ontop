package it.unibz.inf.ontop.answering.reformulation.generation.dialect;

public interface SQLDialectAdapter {


	String getTopNSQL(String sqlQuery, int top);
}
