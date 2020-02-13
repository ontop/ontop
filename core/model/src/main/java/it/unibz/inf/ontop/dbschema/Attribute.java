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
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.Types;
import java.util.Optional;

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
	@Nullable
	private final DBTermType termType;
	private final String typeName;
	private final boolean canNull;
	@Nullable
	private final DBTermType abstractDBType;

	/**
	 * With a term type
	 */
	Attribute(RelationDefinition relation, QualifiedAttributeID id, int index, String typeName,
			  @Nonnull DBTermType termType, boolean canNull) {
		this.table = relation;
		this.index = index;
		this.typeName = typeName;
		this.id = id;
		this.termType = termType;
		this.canNull = canNull;
		this.abstractDBType = null;
	}

	/**
	 * Without a term type
	 */
	Attribute(RelationDefinition relation, QualifiedAttributeID id, int index, String typeName,
			  boolean canNull, DBTypeFactory dbTypeFactory) {
		this.table = relation;
		this.index = index;
		this.typeName = typeName;
		this.id = id;
		this.termType = null;
		this.canNull = canNull;
		abstractDBType = dbTypeFactory.getAbstractRootDBType();
	}

	@JsonProperty("name")
	@JsonSerialize(using = QuotedID.QuotedIDSerializer.class)
	public QuotedID getID() {
		return id.getAttribute();
	}

	@JsonIgnore
	public QualifiedAttributeID getQualifiedID() {
		return id;
	}

	@JsonIgnore
	public RelationDefinition getRelation() {
		return table;
	}

	@JsonIgnore
	public int getIndex() {
		return index;
	}

//	public int getType() {
//		return type;
//	}

	@JsonProperty("isNullable")
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
	@JsonProperty("datatype")
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
			// the same reference(!) for the table
			return this.id.equals(other.id) && (this.table == other.table);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * Returns the precise term type (if defined)
	 */
	@JsonIgnore
	public Optional<DBTermType> getTermType() {
		return Optional.ofNullable(termType);
	}

	/**
	 * May be an abstract term type if no precise term type is defined.
	 *
	 * Intended to be used for VALIDATION ONLY, not for type inference.
	 */
	@JsonIgnore
	public DBTermType getBaseTypeForValidation() {
		return getTermType()
				.orElse(abstractDBType);
	}

	public static class AttributeSerializer extends JsonSerializer<Attribute> {
		@Override
		public void serialize(Attribute value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeString(value.getID().getName());
		}
	}
}
