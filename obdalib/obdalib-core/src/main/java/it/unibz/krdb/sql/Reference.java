package it.unibz.krdb.sql;

/**
 * A utility class to store foreign key information.
 */
public class Reference {
	
	private String pkTableReference;
	private String pkColumnReference;
	
	public Reference(String pkTableReference, String pkColumnReference) {
		this.pkTableReference = pkTableReference;
		this.pkColumnReference = pkColumnReference;
	}
	
	public String getTableReference() {
		return pkTableReference;
	}
	
	public String getColumnReference() {
		return pkColumnReference;
	}
}
