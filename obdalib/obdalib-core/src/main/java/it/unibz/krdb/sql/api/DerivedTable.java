package it.unibz.krdb.sql.api;

/**
 * The DerivedTable class represents the notation of nested query
 * in SQL.
 */
public class DerivedTable implements IRelation {
	
	private IRelation subquery;

	private String alias;

	public DerivedTable(IRelation subquery) {
		setSubQuery(subquery);
	}
	
	public void setSubQuery(IRelation subquery) {
		this.subquery = subquery;
	}
	
	public IRelation getSubQuery() {
		return subquery;
	}

	@Override
	public String toString() {
		return alias;
	}
}