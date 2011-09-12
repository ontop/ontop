package it.unibz.krdb.sql.api;

/**
 * An abstract base class for each term in relational algebra.
 */
public abstract class RelationalAlgebra implements Cloneable {
	
	/**
	 * The SELECT statement.
	 */
	protected Projection projection;
	
	/**
	 * The WHERE statement.
	 */
	protected Selection selection;
	
	/**
	 * The GROUP BY statement.
	 */
	protected Aggregation aggregation;

	public void setProjection(Projection projection) {
		this.projection = projection;
	}

	public Projection getProjection() {
		return projection;
	}

	public void setSelection(Selection selection) {
		this.selection = selection;
	}

	public Selection getSelection() {
		return selection;
	}

	public void setAggregation(Aggregation aggregation) {
		this.aggregation = aggregation;
	}

	public Aggregation getAggregation() {
		return aggregation;
	}
	
	public abstract RelationalAlgebra clone();
}