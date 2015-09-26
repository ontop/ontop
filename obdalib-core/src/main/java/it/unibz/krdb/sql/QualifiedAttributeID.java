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


/**
 * Database identifier used for possibly qualified column names
 * <p>
 * Relation can be empty (null)
 * 
 * @author Roman Kontchakov
 *
 */


public class QualifiedAttributeID {

	private final QuotedID attribute;
	private final RelationID relation;
	
	public QualifiedAttributeID(RelationID relation, QuotedID attribute) {
		this.relation = relation;
		this.attribute = attribute;
	}
	
	public QuotedID getAttribute() {
		return attribute;
	}
	
	public RelationID getRelation() {
		return relation;
	}
	
	public String getSQLRendering() {
		return ((relation == null) ? "" : (relation.getSQLRendering() + ".")) + attribute.getSQLRendering();
	}
	
	@Override 
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj instanceof QualifiedAttributeID) {
			QualifiedAttributeID other = (QualifiedAttributeID)obj;
			return (this.attribute.equals(other.attribute) && 
					((this.relation == other.relation) || 
							((this.relation != null) && this.relation.equals(other.relation))));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return attribute.hashCode();
	}
	
	@Override
	public String toString() {
		return getSQLRendering();
	}
}
