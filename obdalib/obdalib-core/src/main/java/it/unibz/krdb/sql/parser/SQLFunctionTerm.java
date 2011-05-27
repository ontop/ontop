package it.unibz.krdb.sql.parser;

public class SQLFunctionTerm implements ISQLTerm{

	private String function = null;
	private ISQLTerm term = null;
	
	public SQLFunctionTerm(String f, ISQLTerm t){
		function = f;
		term = t;
	}
	
	public String toString(){
		
		return function + "(" + term.toString() +")";
	}
}
