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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class DataDefinition implements Serializable {

	private static final long serialVersionUID = 212770563440334334L;

	private String name;

	protected final Map<Integer, Attribute> attributes = new HashMap<>();

	protected DataDefinition(String name) {
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

	public Collection<Attribute> getAttributes() {
		return attributes.values();
	}
	
	/**
	 * Used to get the key of an attribute, so it can be get or set with the {@link #getAttribute(int) getAttribute} or {@link #setAttribute(int, Attribute) setAttribute}
	 * 
	 * @param The name of an attribute, correctly spelled and cased.
	 * @return The key in the hashmap
	 */
	public int getAttributeKey(String attributeName) {
        for (int idx : attributes.keySet()) {
            if (attributes.get(idx).hasName(attributeName)) {
                return idx;
            }
        }
        return -1;
    }
	

	public int getNumOfAttributes() {
		return attributes.size();
	}
}
