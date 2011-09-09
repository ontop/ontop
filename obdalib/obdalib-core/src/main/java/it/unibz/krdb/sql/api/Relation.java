package it.unibz.krdb.sql.api;

public class Relation extends RelationalAlgebra {
	
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
