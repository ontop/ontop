package it.unibz.krdb.sql.api;


/**
 * The Relation class is a wrapper class that make the
 * {@link TablePrimary} class compatible with the 
 * abstraction in the {@link QueryTree}.
 */
public class Relation extends RelationalAlgebra {
	
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
}
