package it.unibz.krdb.sql.api;

import java.io.Serializable;

/**
 * An abstract base class for each term in relational algebra.
 */
public abstract class RelationalAlgebra implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 6661455497184268723L;

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