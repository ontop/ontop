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
 * Database identifier used for possibly qualified table names and aliases
 * <p>
 * Schema name can be empty (null)
 * 
 * @author Roman Kontchakov
 *
 */


public class RelationID {

	private final QuotedID schema, table;
	
	RelationID(QuotedID schema, QuotedID table) {
		this.schema = (schema == null) ? new QuotedID(null, "") : schema;
		this.table = table;
	}
	
	public RelationID getSchemalessID() {
		return new RelationID(null, table);
	}
	
	public boolean hasSchema() {
		return schema.getName() != null;
	}
	
	public String getSchemaSQLRendering() {
		return schema.getSQLRendering();
	}

	public String getTableNameSQLRendering() {
		return table.getSQLRendering();
	}

	public String getSchemaName() {
		return schema.getName();
	}
	
	public String getTableName() {
		return table.getName();
	}
	
	public String getDatalogPredicateName() {
		String s = schema.getName();
		if (s == null)
			return table.getName();
		
		return s + "." + table.getName();
	}
	
	/**
	 * 
	 * @param s a predicate-name rendering of a possibly qualified table name
	 * @return
	 */
	
	
	public static RelationID createRelationFromPredicateName(String s) {
		String[] names = s.split("\\.");
		if (names.length == 1)
			return new RelationID(new QuotedID(null, QuotedID.QUOTATION), new QuotedID(s, QuotedID.QUOTATION));
		else
			return new RelationID(new QuotedID(names[0], QuotedID.QUOTATION), new QuotedID(names[1], QuotedID.QUOTATION));			
	}

	/**
	 * 
	 * @param schema as is in DB (possibly null)
	 * @param table as is in DB
	 * @return
	 */
	
	public static RelationID createRelationFromDatabaseRecord(String schema, String table) {
		return new RelationID(new QuotedID(schema, QuotedID.QUOTATION), new QuotedID(table, QuotedID.QUOTATION));			
	}


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
}
