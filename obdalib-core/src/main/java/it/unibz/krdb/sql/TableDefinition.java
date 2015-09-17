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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

public class TableDefinition extends DataDefinition {

	private static final long serialVersionUID = 1L;

	public TableDefinition(String name) {
		super(name);
	}
/*	
	public Map<String, List<Attribute>> getForeignKeys() {
		Map<String, List<Attribute>> foreignKeys = new HashMap<>();
		for (Attribute attr : getAttributes()) {
			Reference ref = attr.getReference();
			if (ref != null) {
				String fkName = ref.getReferenceName();
				List<Attribute> fkAttributes = foreignKeys.get(fkName);
				if (fkAttributes == null) {
					fkAttributes = new ArrayList<>();
					foreignKeys.put(fkName, fkAttributes);
				}
				fkAttributes.add(attr);
			}
		}
		return foreignKeys;
	}
*/	
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
			if (getPrimaryKey() != null && getPrimaryKey().getAttributes().contains(att)) 
				bf.append(":PK");
			
			/*
			Reference ref = att.getReference();
			if (ref != null) {
				bf.append(":FK:");
				bf.append(String.format("%s(%s)", 
						ref.getTableReference(), 
						ref.getColumnReference()));
			}
			*/
			comma = true;
		}
		bf.append("]");
		return bf.toString();
	}
}
