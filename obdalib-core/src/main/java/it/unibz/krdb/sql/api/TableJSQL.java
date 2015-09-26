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

import it.unibz.krdb.sql.QuotedID;
import it.unibz.krdb.sql.RelationID;

import java.io.Serializable;

/**
 * Class TableJSQL used to store the information about the tables. 
 * We distinguish between givenName and Name.
 * Since with Name we don't want to consider columns.
 */

public class TableJSQL implements Serializable{
	
	private static final long serialVersionUID = 7031993308873750327L;
	
	private final RelationID table;
	private final QuotedID alias;
	private final String tableGivenName;
	
	public TableJSQL(RelationID table, QuotedID alias) {
		this.table = table;
		// table FQN is optional schema and "." followed by table name 
		this.tableGivenName =  table.getSQLRendering();
		this.alias = alias;		
	}

	public String getTableGivenName() {
		return tableGivenName;
	}
	
	
	public RelationID getTable() {
		return table;
	}

	public QuotedID getAlias() {
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
