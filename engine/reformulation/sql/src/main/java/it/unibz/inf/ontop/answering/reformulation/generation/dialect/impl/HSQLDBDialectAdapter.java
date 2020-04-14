package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


public class HSQLDBDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String getSQLLexicalFormString(String constant) {
		return "'" + constant + "'";
	}
	
}
