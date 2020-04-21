package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


public class MySQLDialectAdapter extends SQL99DialectAdapter {


    @Override
    public String getSQLLexicalFormString(String constant) {
		// quotes, doubles backslashes and escapes single quotes
		return "'" + constant.replace("\\", "\\\\")
				.replaceAll("(?<!')'(?!')", "\\'") + "'";
    }
}
