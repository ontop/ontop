package it.unibz.krdb.sql;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class DataDefinition implements Serializable {

	private static final long serialVersionUID = 212770563440334334L;

	protected String name;

	protected HashMap<Integer, Attribute> attributes = new HashMap<Integer, Attribute>();

	public DataDefinition() { // TODO Remove later! The attribute name should be mandatory and cannot be changed!
		// NO-OP
	}
	
	public DataDefinition(String name) {
		this.name = name;
	}

	public void setName(String name) { // TODO Remove later! The attribute name should be mandatory and cannot be changed!
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setAttribute(int pos, Attribute value) {
		attributes.put(pos, value);
	}

	public String getAttributeName(int pos) {
		Attribute attribute = attributes.get(pos);
		return attribute.getName();
	}

	public Attribute getAttribute(int pos) {
		Attribute attribute = attributes.get(pos);
		return attribute;
	}

	public ArrayList<Attribute> getAttributes() {
		ArrayList<Attribute> list = new ArrayList<Attribute>();
		for (Attribute value : attributes.values()) {
			list.add(value);
		}
		return list;
	}

	public int getAttributePosition(String attributeName) {
		int index = 0;
		for (Attribute value : attributes.values()) {
			if (value.hasName(attributeName)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	public int countAttribute() {
		return attributes.size();
	}
}
