package it.unibz.krdb.sql.api;

/**
 * The Relation class is a wrapper class that make the
 * {@link TablePrimary} class compatible with the 
 * abstraction in the {@link QueryTree}.
 */
public class Relation extends RelationalAlgebra {

	private static final long serialVersionUID = 8464933976477745339L;
	
	private TablePrimary table;
	
	public Relation(TablePrimary table) {
		this.table = table;
	}
	
	public String getSchema() {
		return table.getSchema();
	}
	
	public String getName() {
		return table.getName();
	}
	
	public String getTableName() {
		return table.getTableName();
	}
	
	public String getGivenName() {
		return table.getGivenName();
	}
	public String getAlias() {
		return table.getAlias();
	}
	
	@Override
	public String toString() {
		return table.toString();
	}
	
	@Override
	public Relation clone() {
		return new Relation(table);
	}
	
	@Override
	public boolean equals(Object r){
		if(r instanceof Relation)
			return this.table.equals(((Relation)r).table);
		return false;
	}
}
