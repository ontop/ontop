package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


public class HSQLDBDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String sqlQuote(String name) {
		//TODO: This should depend on quotes in the sql in the mappings
		return String.format("\"%s\"", name);
//		return name;
	}

	@Override
	public String getSQLLexicalFormString(String constant) {
		return "'" + constant + "'";
	}
	
}
