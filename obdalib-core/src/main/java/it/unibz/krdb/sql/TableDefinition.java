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

import it.unibz.krdb.sql.api.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableDefinition extends DataDefinition {

	private static final long serialVersionUID = 1L;

	public TableDefinition(String name) {
		super(name);
	}
	
	public List<Attribute> getPrimaryKeys() {
		List<Attribute> primaryKeys = new ArrayList<Attribute>();
		for (Attribute attr : attributes.values()) {
			if (attr.isPrimaryKey()) {
				primaryKeys.add(attr);
			}
		}
		return primaryKeys;
	}
	
	public Map<String, List<Attribute>> getForeignKeys() {
		Map<String, List<Attribute>> foreignKeys = new HashMap<String, List<Attribute>>();
		for (Attribute attr : attributes.values()) {
			if (attr.isForeignKey()) {
				String fkName = attr.getReference().getReferenceName();
				List<Attribute> fkAttributes = foreignKeys.get(fkName);
				if (fkAttributes == null) {
					fkAttributes = new ArrayList<Attribute>();
					foreignKeys.put(fkName, fkAttributes);
				}
				fkAttributes.add(attr);
			}
		}
		return foreignKeys;
	}
	
	@Override
	public String toString() {
		StringBuilder bf = new StringBuilder();
		bf.append(getName());
		bf.append("[");
		boolean comma = false;
		for (Integer i : attributes.keySet()) {
			if (comma) {
				bf.append(",");
			}
			Attribute at = attributes.get(i);
			bf.append(at);
			if (at.isPrimaryKey()) {
				bf.append(":PK");
			}
			if (at.isForeignKey()) {
				bf.append(":FK:");
				bf.append(String.format("%s(%s)", 
						at.getReference().getTableReference(), 
						at.getReference().getColumnReference()));
			}
			comma = true;
		}
		bf.append("]");
		return bf.toString();
	}
}
