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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import it.unibz.inf.ontop.dbschema.impl.QuotedIDImpl;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.io.IOException;

/**
 * Represents an attribute (column) of a database relation (table or view) or a parser view
 *
 * @author Roman Kontchakov
 *
 */

public class Attribute {

	private final RelationDefinition relation;

	private final QuotedID id;
	private final int index;
	private final DBTermType termType;
	private final String typeName;
	private final boolean isNullable;

	/**
	 * With a term type
	 */
	Attribute(RelationDefinition relation, QuotedID id, int index, String typeName,
			  DBTermType termType, boolean isNullable) {
		this.relation = relation;
		this.id = id;
		this.index = index;
		this.typeName = typeName;
		this.termType = termType;
		this.isNullable = isNullable;
	}

	@JsonIgnore
	public RelationDefinition getRelation() {
		return relation;
	}

	@JsonProperty("name")
	@JsonSerialize(using = QuotedIDImpl.QuotedIDSerializer.class)
	public QuotedID getID() {
		return id;
	}

	/**
	 * @return the index (starting from 1)
	 */
	@JsonIgnore
	public int getIndex() { return index; }

	@JsonProperty("isNullable")
	public boolean isNullable() { return isNullable; }

	/**
	 * @return the name of the SQL type associated with this attribute.
	 */
	@JsonProperty("datatype")
	public String getSQLTypeName() {
		return typeName;
	}

	/**
	 * @return the precise term type
	 */
	@JsonIgnore
	public DBTermType getTermType() { return termType; }

	@Override
	public String toString() {
		return id + " " + typeName + (isNullable ? "" : " NOT NULL");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj instanceof Attribute) {
			Attribute other = (Attribute)obj;
			// the same reference(!) for the relation
			return this.id.equals(other.id) && (this.relation == other.relation);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode(); // never mix attributes from different relations
	}

	public static class AttributeSerializer extends JsonSerializer<Attribute> {
		@Override
		public void serialize(Attribute value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeString(value.getID().getSQLRendering());
		}
	}
}
