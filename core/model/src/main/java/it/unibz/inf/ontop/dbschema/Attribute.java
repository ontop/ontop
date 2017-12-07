package it.unibz.inf.ontop.dbschema;

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

import it.unibz.inf.ontop.model.type.TermType;

import java.sql.Types;

/**
 * Represents an attribute (column) of a database relation (table or view) or a parser view
 * 
 * @author Roman Kontchakov
 *
 */

public class Attribute {

	private final RelationDefinition table; // reference to the relation or parser view
	
	private final QualifiedAttributeID id; // qualified id (table = tableId for database relation
	                                       //               parser views, however, have properly qualified column names
	private final int index;
	private final int type;
	private final String typeName;
	private final boolean canNull;
	private final TermType termType;

	Attribute(RelationDefinition relation, QualifiedAttributeID id, int index, int type, String typeName, boolean canNull,
			  TermType termType) {
		this.table = relation;
		this.index = index;
		this.id = id;
		this.type = type;
		this.typeName = typeName;
		this.canNull = canNull;
		this.termType = termType;
	}
	
	public QuotedID getID() {
		return id.getAttribute();
	}
	
	public QualifiedAttributeID getQualifiedID() {
		return id;
	}
	
	public RelationDefinition getRelation() {
		return table;
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getType() {
		return type;
	}
	
	public boolean canNull() {
		return canNull;
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
		return id.getAttribute() + " " + typeName + (!canNull ? " NOT NULL" : "");
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj instanceof Attribute) {
			Attribute other = (Attribute)obj;
			//                                 the same reference(!) for the table
			return this.id.equals(other.id) && (this.table == other.table);  
		}
		
		return false;
	}
	
	@Override 
	public int hashCode() {
		return id.hashCode();
	}

	public TermType getTermType() {
		return termType;
	}
}
