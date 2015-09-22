package it.unibz.krdb.sql.api;

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

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.io.Serializable;

/**
 * Class TableJSQL used to store the information about the tables. 
 * We distinguish between givenName and Name.
 * Since with Name we don't want to consider columns.
 */

public class TableJSQL implements Serializable{
	
	private static final long serialVersionUID = 7031993308873750327L;
	
	public static final class Identifier {

		// These  fields are to handle quoted names in multischema.
		// givenSchema and givenName are the names with quotes, upper case,
		// that is, exactly as given by the user. 
		// quotedTable and quotedSchema are boolean value to identify when quotes have been removed.
		private final String name;
		private final boolean quoted;
	
		private Identifier(String name) {
			if (name != null && ParsedSQLQuery.pQuotes.matcher(name).matches()) {
	            this.name = name.substring(1, name.length() - 1);
	            this.quoted = true;
	        }
			else {
				this.name = name;
				this.quoted = false;
			}
		}
		
		public boolean isQuoted() {
			return quoted;
		}
		
		public String getName() {
			return name;
		}
	};
	
	private final Identifier schema, table;
	private final Alias alias;
	private final String tableGivenName;
	
	public TableJSQL(String schemaName, String tableName, Alias a) {
		this.schema = new Identifier(schemaName);
		this.table = new Identifier(tableName);
		// table FQN is optional schema and "." followed by table name 
		this.tableGivenName =  (schemaName == null) ? tableName : schemaName + "." + tableName;
		this.alias = a;		
		if (alias != null) 
			unQuoteAlias(alias);
	}

	public String getTableGivenName() {
		return tableGivenName;
	}
	
	/**
	 * unquote both the column and table names (including the schema and alias)
	 * and STORE them back
	 * 
	 * @param tableColumn
	 */
	
	public static void unquoteColumnAndTableName(Column tableColumn) {
		unquoteColumnName(tableColumn);
		
		Table table = tableColumn.getTable();
		if (table != null) {
			TableJSQL fixTable = new TableJSQL(table.getSchemaName(), table.getName(), table.getAlias());
			table.setAlias(fixTable.getAlias());
			table.setName(fixTable.getTable().getName());
			table.setSchemaName(fixTable.getSchema().getName());		
		}
	}
	
	/**
	 * unquote the column name and STORE the unquoted name back
	 * 
	 * @param column
	 * @return
	 */
	
	public static String unquoteColumnName(Column column) {
		String columnName = column.getColumnName();
		
		if (ParsedSQLQuery.pQuotes.matcher(columnName).matches()) {
			columnName = columnName.substring(1, columnName.length() - 1);
			column.setColumnName(columnName);
		}
		return columnName;
	}

	/**
	 * unquote the alias name and STORE the unqouted name back
	 * 
	 * @param a
	 */
	
	public static void unQuoteAlias(Alias a) {
		String name = a.getName();
		if (ParsedSQLQuery.pQuotes.matcher(name).matches()) {
			a.setName(name.substring(1, name.length() - 1));
		}
	}
	
	public Identifier getSchema() {
		return schema;
	}

	public Identifier getTable() {
		return table;
	}

	public Alias getAlias() {
		return alias;
	}
	
	@Override
	public String toString() {
		return tableGivenName;
	}

	/**
	 * Called from the MappingParser:getTables. 
	 * Needed to remove duplicates from the list of tables
	 */
	@Override
	public boolean equals(Object t) {
		if (t instanceof TableJSQL) {
			TableJSQL tp = (TableJSQL) t;
			return this.tableGivenName.equals(tp.tableGivenName)
					&& ((this.alias == tp.alias)
							|| ((this.alias != null) && this.alias.equals(tp.alias)));
		}
		return false;
	}
}
