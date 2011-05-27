package it.unibz.krdb.sql.parser;

public class SQLNummericConstant implements ISQLTerm {

	private String value = null;
	
	public SQLNummericConstant(String v){
		value = v;
	}
	
	public String toString(){
		return value;
	}
}
