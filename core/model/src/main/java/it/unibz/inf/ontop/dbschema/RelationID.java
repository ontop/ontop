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


/**
 * Database identifier used for possibly qualified table names and aliases
 * <p>
 * Schema name can be empty
 *
 * @author Roman Kontchakov
 *
 */

public interface RelationID {

	/**
	 *
	 * @return the relation ID that has the same name but no schema name
	 */
	RelationID getSchemalessID();

	/**
	 *
	 * @return true if the relation ID contains schema
	 */
	boolean hasSchema();

	/**
	 *
	 * @return null if the schema name is empty or SQL rendering of the schema name (possibly in quotation marks)
	 */
	String getSchemaSQLRendering();

	/**
	 *
	 * @return SQL rendering of the table name (possibly in quotation marks)
	 */
	String getTableNameSQLRendering();

	/**
	 *
	 * @return null if the schema name is empty or the schema name (as is, without quotation marks)
	 */
	String getSchemaName();

	/**
	 *
	 * @return table name (as is, without quotation marks)
	 */
	String getTableName();

	/**
	 *
	 * @return SQL rendering of the name (possibly with quotation marks)
	 */
	String getSQLRendering();
}
