package it.unibz.krdb.sql.api;

public class Attribute {
	
	/** Fields */
	public String name;
	public String type;
	public boolean bPrimaryKey;
	public int canNull;

	public Attribute(String name) {
		this(name, "", false, 0);
	}

	public Attribute(String name, String type) {
		this(name, type, false, 0);
	}

	public Attribute(String name, String type, boolean primaryKey) {
		this(name, type, primaryKey, 0);
	}
	
	public Attribute(String name, String type, boolean primaryKey, int canNull) {
		this.name = name;
		this.type = type;
		this.bPrimaryKey = primaryKey;
		this.canNull = canNull;
	}
	
	/**
	 * Determines whether this attribute object contains a
	 * specified name.
	 * 
	 * @param name
	 * 			The name in question.
	 * @return Returns true if the attribute has the name,
	 * or false, otherwise.
	 */
	public boolean hasName(String name) {
		return (this.name == name)? true : false;
	}
}