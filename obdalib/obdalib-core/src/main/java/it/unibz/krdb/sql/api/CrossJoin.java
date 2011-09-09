package it.unibz.krdb.sql.api;

public class CrossJoin extends JoinOperator {

	public CrossJoin() {
		super();
	}
	
	@Override
	public String toString() {
		return "%s, %s";
	}	
}
