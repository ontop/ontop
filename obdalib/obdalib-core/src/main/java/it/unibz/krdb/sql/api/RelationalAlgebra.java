package it.unibz.krdb.sql.api;

public abstract class RelationalAlgebra implements Cloneable {
	
	protected IRelation table;

	protected Projection projection;
	protected Selection selection;
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