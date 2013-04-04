package it.unibz.krdb.sql.api;

import it.unibz.krdb.sql.Reference;

import java.io.Serializable;

public class Attribute implements Serializable{
	
	private static final long serialVersionUID = -5780621780592347583L;
	
	/** Fields */
	private String name;
	private int type;
	private boolean bPrimaryKey;
	private Reference foreignKey;
	private int canNull;
	
	public String toString() {
		return name + ":" + type;
	}
	
	public Attribute(String name) {
		this(name, 0, false, null, 0);
	}

	public Attribute(String name, int type) {
		this(name, type, false, null, 0);
	}
	
	/**
	 * Use Attribute(String name, int type, boolean primaryKey, Reference foreignKey) instead.
	 */
	@Deprecated
	public Attribute(String name, int type, boolean primaryKey, boolean foreignKey) {
		this(name, type, primaryKey, null, 0);
	}
	
	public Attribute(String name, int type, boolean primaryKey, Reference foreignKey) {
		this(name, type, primaryKey, foreignKey, 0);
	}

	/**
	 * Use Attribute(String name, int type, boolean primaryKey, Reference foreignKey, int canNull) instead.
	 */
	@Deprecated
	public Attribute(String name, int type, boolean primaryKey, boolean foreignKey, int canNull) {
		this(name, type, primaryKey, null, 0);
	}
	
	public Attribute(String name, int type, boolean primaryKey, Reference foreignKey, int canNull) {
		this.name = name;
		this.type = type;
		this.bPrimaryKey = primaryKey;
		this.foreignKey = foreignKey;
		this.canNull = canNull;
	}
	
	public String getName() {
		return name;
	}
	
	public int getType() {
		return type;
	}
	
	public boolean isPrimaryKey() {
		return bPrimaryKey;
	}
	
	public boolean isForeignKey() {
		return foreignKey != null;
	}
	
	public boolean canNull() {
		return canNull == 1;
	}
	
	public Reference getReference() {
		return foreignKey;
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
		return (this.name.equals(name)) ? true : false;
	}
}