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
		private final String givenName;
		private final String name;
		private final boolean quoted;
	
		private Identifier(String name, String givenName) {
			if (name != null && ParsedSQLQuery.pQuotes.matcher(name).matches()) {
	            this.name = name.substring(1, name.length() - 1);
	            this.quoted = true;
	        }
			else {
				this.name = name;
				this.quoted = false;
			}
				
			this.givenName = givenName;
		}
		
		public boolean isQuoted() {
			return quoted;
		}
		
		public String getName() {
			return name;
		}
		
		public String getGivenName() {
			return givenName;
		}
	};
	
	private final Identifier schema, table;
	private final Alias alias;
	
	/**
	 * This constructor is used when we take the table names from the mappings.
	 */
	public TableJSQL(String schemaName, String tableName, String givenName) {
		this.schema = new Identifier(schemaName, schemaName);
		this.table = new Identifier(tableName, givenName);
		this.alias = null;
	}
	
	public TableJSQL(Table table) {
		this.schema = new Identifier(table.getSchemaName(), table.getSchemaName());
		this.table = new Identifier(table.getName(), table.getFullyQualifiedName());
		Alias a = table.getAlias();
		if (a != null) {
			String name = a.getName();
			// unquote the alias name AND MODIFY IT (ROMAN 22 Sep 2015)
			if (ParsedSQLQuery.pQuotes.matcher(name).matches()) {
				a.setName(name.substring(1, name.length() - 1));
			}
		}
		this.alias = a;		
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
		return table.givenName;
	}

	/**
	 * Called from the MappingParser:getTables. 
	 * Needed to remove duplicates from the list of tables
	 */
	@Override
	public boolean equals(Object t) {
		if (t instanceof TableJSQL) {
			TableJSQL tp = (TableJSQL) t;
			return this.table.givenName.equals(tp.table.givenName)
					&& ((this.alias == tp.alias)
							|| ((this.alias != null) && this.alias.equals(tp.alias)));
		}
		return false;
	}
}
