package it.unibz.krdb.sql.api;

/**
 * The DerivedTable class represents the notation of nested query
 * in SQL.
 */
public class DerivedTable implements ITable {
	
	private static final long serialVersionUID = -8310536644014311978L;

	private QueryTree subquery;

	private String alias;

	public DerivedTable(QueryTree subquery) {
		setSubQuery(subquery);
	}
	
	public void setSubQuery(QueryTree subquery) {
		this.subquery = subquery;
	}
	
	public QueryTree getSubQuery() {
		return subquery;
	}

	@Override
	public String toString() {
		return alias;
	}
}