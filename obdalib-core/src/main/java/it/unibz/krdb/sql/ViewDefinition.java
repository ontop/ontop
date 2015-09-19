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

public class ViewDefinition extends RelationDefinition {

	private final String statement;
	
	public ViewDefinition(String catalogName, String schemaName, String tableName, String name, String statement) {
		super(catalogName, schemaName, tableName, name);
		this.statement = statement;
	}

	public String getStatement() {
		return statement;
	}

	@Override
	public String toString() {
		StringBuilder bf = new StringBuilder();
		bf.append(getName());
		bf.append("[");
		boolean comma = false;
		for (Attribute att : getAttributes()) {
			if (comma) 
				bf.append(",");
			
			bf.append(att);
			comma = true;
		}
		bf.append("]");

		bf.append(String.format("   (%s)", statement));
		return bf.toString();
	}
}
