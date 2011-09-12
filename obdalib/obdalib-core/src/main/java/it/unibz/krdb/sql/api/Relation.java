package it.unibz.krdb.sql.api;

/**
 * A base class for relations.
 */
public class Relation extends RelationalAlgebra {
	
	private IRelation table;
	
	public Relation(IRelation table) {
		this.table = table;
	}
	
	@Override
	public String toString() {
		return table.toString();
	}
	
	@Override
	public Relation clone() {
		return null;
	}
}
