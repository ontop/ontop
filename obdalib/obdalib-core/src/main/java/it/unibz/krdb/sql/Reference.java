package it.unibz.krdb.sql;

/**
 * A utility class to store foreign key information.
 */
public class Reference {
	private String fkReferenceName;
	private String pkTableReference;
	private String pkColumnReference;
	
	public Reference(String fkReferenceName, String pkTableReference, String pkColumnReference) {
		this.fkReferenceName = fkReferenceName;
		this.pkTableReference = pkTableReference;
		this.pkColumnReference = pkColumnReference;
	}
	
	public String getReferenceName() {
		return fkReferenceName;
	}
	
	public String getTableReference() {
		return pkTableReference;
	}
	
	public String getColumnReference() {
		return pkColumnReference;
	}
}
