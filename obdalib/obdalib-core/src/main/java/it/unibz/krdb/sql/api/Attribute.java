package it.unibz.krdb.sql.api;

public class Attribute {
	
	/** Fields */
	public final String name;
	public final int type;
	public final boolean bPrimaryKey;
	public final int canNull;
	
	
	public String toString() {
		return name + ":" + type;
	}
	
	public Attribute(String name) {
		this(name, 0, false, 0);
	}

	public Attribute(String name, int type) {
		this(name, type, false, 0);
	}

	public Attribute(String name, int type, boolean primaryKey) {
		this(name, type, primaryKey, 0);
	}
	
	public Attribute(String name, int type, boolean primaryKey, int canNull) {
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