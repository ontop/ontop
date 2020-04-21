package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


public class MySQLDialectAdapter extends SQL99DialectAdapter {



	@Override
	public String escapedSingleQuote(){ return "\\'"; }

	/**
	 * Also doubles anti-slashes
	 */
    @Override
    public String getSQLLexicalFormString(String constant) {
        //return "'" + constant.replace("\\", "\\\\").replace("'", escapedSingleQuote()) + "'";
        return super.getSQLLexicalFormString(constant.replace("\\", "\\\\"));
    }
}
