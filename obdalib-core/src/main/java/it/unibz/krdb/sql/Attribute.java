package it.unibz.krdb.sql;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.sql.Types;

public class Attribute implements Serializable{
	
	private static final long serialVersionUID = -5780621780592347583L;
	
	/** Fields */
	private final String name;
	private final int type;
	private final int canNull;
	private final String typeName;
	
	private boolean bPrimaryKey;
	private Reference foreignKey;

	private boolean unique;

	public Attribute(String name) {
		this(name, 0, false, null, 0, null, false);
	}

	public Attribute(String name, int type, boolean primaryKey, Reference foreignKey, int canNull, String typeName, boolean unique) {
		this.name = name;
		this.type = type;
		this.bPrimaryKey = primaryKey;
		this.foreignKey = foreignKey;
		this.canNull = canNull;
		this.typeName = typeName;
        this.unique = unique;
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
	
	public boolean canNull() {
		return canNull == 1;
	}
	
	public Reference getReference() {
		return foreignKey;
	}
	
	/***
	 * Returns the name of the SQL type associated with this attribute. Note, the name maybe not match
	 * the integer SQL id. The integer SQL id comes from the {@link Types} class, and these are few. Often
	 * databases match extra datatypes they may provide to the same ID, e.g., in MySQL YEAR (which doesn't
	 * exists in standard SQL, is mapped to 91, the ID of DATE. This field helps in disambiguating this 
	 * cases.
	 * 
	 * @return
	 */
	public String getSQLTypeName() {
		return typeName;
	}
	
	@Override
	public String toString() {
		return name + ":" + type;
	}

    public boolean isUnique() {
        return unique;
    }
}
