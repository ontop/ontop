package it.unibz.krdb.sql.api;

/**
 * This class represents the cross join operation in the
 * SQL query.
 */
public class CrossJoin extends JoinOperator {

	public CrossJoin() {
		super();
	}
	
	@Override
	public String toString() {
		return "%s, %s";
	}	
}
