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

import java.io.IOException;

/**
 * Database identifier used for possibly qualified table names and aliases
 * <p>
 * Schema name can be empty
 *
 * @author Roman Kontchakov
 *
 */

//@JsonSerialize(using = RelationID.RelationIDSerializer.class)
public class RelationID {

	private final QuotedID schema, table;

	/**
	 * (used only in QuotedIDFactory implementations)
	 *
	 * @param schema
	 * @param table
	 */

	protected RelationID(QuotedID schema, QuotedID table) {
		this.schema = schema;
		this.table = table;
	}

	/**
	 * creates relation id from the database record (as though it is quoted)
	 *
	 * @param schema as is in DB (possibly null)
	 * @param table as is in DB
	 * @return
	 */

	public static RelationID createRelationIdFromDatabaseRecord(QuotedIDFactory idfac, String schema, String table) {
		// both IDs are as though they are quoted -- DB stores names as is
		return new RelationID(QuotedID.createIdFromDatabaseRecord(idfac, schema),
								QuotedID.createIdFromDatabaseRecord(idfac, table));
	}

	/**
	 *
	 * @return the relation ID that has the same name but no schema name
	 */
	@JsonIgnore
	public RelationID getSchemalessID() {
		return new RelationID(QuotedID.EMPTY_ID, table);
	}

	/**
	 *
	 * @return true if the relation ID contains schema
	 */
	public boolean hasSchema() {
		return schema.getName() != null;
	}

	/**
	 *
	 * @return null if the schema name is empty or SQL rendering of the schema name (possibly in quotation marks)
	 */
	@JsonIgnore
	public String getSchemaSQLRendering() {
		return schema.getSQLRendering();
	}

	/**
	 *
	 * @return SQL rendering of the table name (possibly in quotation marks)
	 */
	@JsonIgnore
	public String getTableNameSQLRendering() {
		return table.getSQLRendering();
	}

	/**
	 *
	 * @return null if the schema name is empty or the schema name (as is, without quotation marks)
	 */
	@JsonProperty("schema")
	public String getSchemaName() {
		return schema.getName();
	}

	/**
	 *
	 * @return table name (as is, without quotation marks)
	 */

	@JsonProperty("name")
	public String getTableName() {
		return table.getName();
	}

	/**
	 *
	 * @return SQL rendering of the name (possibly with quotation marks)
	 */

	public String getSQLRendering() {
		String s = schema.getSQLRendering();
		if (s == null)
			return table.getSQLRendering();

		return s + "." + table.getSQLRendering();
	}

	@Override
	public String toString() {
		return getSQLRendering();
	}

	@Override
	public int hashCode() {
		return table.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj instanceof RelationID) {
			RelationID other = (RelationID)obj;
			return (this.schema.equals(other.schema) && this.table.equals(other.table));
		}

		return false;
	}

	static class RelationIDSerializer extends JsonSerializer<RelationID> {

		@Override
		public void serialize(RelationID value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeString(value.getSQLRendering());
		}
	}
}
