package it.unibz.krdb.sql.parser;

public class SQLTable {

	private String name = null;
	private String schema= null;
	private String alias= null;
	
	public SQLTable(String s, String n, String a){
		name = n;
		schema = s;
		alias = a;
	}

	public String getName() {
		return name;
	}

	public String getSchema() {
		return schema;
	}

	public String getAlias() {
		return alias;
	}
	
	public String toString(){
		
		String aux = "";
		if(schema != null){
			aux = aux + schema+".";
		}
		aux = aux + name;
		if(alias != null){
			aux = aux + " as " + alias;
		}
		
		return aux;
	}
}
