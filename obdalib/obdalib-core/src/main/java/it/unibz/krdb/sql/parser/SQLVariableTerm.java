package it.unibz.krdb.sql.parser;

public class SQLVariableTerm implements ISQLTerm {

	private String name = null;
	private String tablename = null;
	
	public SQLVariableTerm(String table, String name){
		this.name = name;
		tablename= table;
	}
	
	public String toString(){
		
		if(tablename == null){
			return name;
		}else{
			return tablename +"."+name;
		}
	}
}
