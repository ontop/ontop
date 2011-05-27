package it.unibz.krdb.sql.parser;

public class SQLLiteralConstant implements ISQLTerm {

	private String name =null;
	
	public SQLLiteralConstant(String s){
		name = s;
	}
	
	public String toString(){
		
		return "'" + name +"'";
	} 
}
