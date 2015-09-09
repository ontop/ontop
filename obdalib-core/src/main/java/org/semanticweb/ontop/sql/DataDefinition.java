package org.semanticweb.ontop.sql;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.semanticweb.ontop.sql.api.Attribute;

/**
 * TODO: see if we can make it immutable so that it can be shared between threads.
 */

public abstract class DataDefinition implements Serializable {

	private static final long serialVersionUID = 212770563440334334L;

	private final String name;
	private final List<Attribute> attributes;

	protected DataDefinition(String name, List<Attribute> attributes) {
		this.name = name;
        this.attributes = new ArrayList<>(attributes);
	}

	public String getName() {
		return name;
	}

	public void addAttribute(Attribute value) {
		attributes.add(value);
	}

    /**
     * This method should be overwritten by subclasses
     */
    abstract public DataDefinition cloneDefinition();

	// TODO: remove from ImplicitDBConstraints
	@Deprecated
	public void setAttribute(int pos, Attribute value) {
        // indexes start at 1
        int index = pos - 1;
        if (index >= attributes.size()) {
            attributes.add(value);
        }
        else {
            attributes.set(index, value);
        }
    }
	public String getAttributeName(int pos) {
		if (attributes.size() < pos)
			throw new IllegalArgumentException("No attribute at this position: " + pos + " " + attributes);
        Attribute attribute = attributes.get(pos - 1);
		return attribute.getName();
	}
	
	/**
	 * gets attribute with the specified position
	 * 
	 * @param pos is position <em>staring at 1</em>
	 * @return attribute at the position
	 */
	
	public Attribute getAttribute(int pos) {
		// positions start at 1
		Attribute attribute = attributes.get(pos - 1);
		return attribute;
	}

	public List<Attribute> getAttributes() {
		return Collections.unmodifiableList(attributes);
	}
	
	/**
	 * Used to get the key of an attribute, so it can be get or set with the {@link #getAttribute(int) getAttribute} or {@link #setAttribute(int, Attribute) setAttribute}
	 * 
	 * @param The name of an attribute, correctly spelled and cased.
	 * @return The key in the hashmap
	 */
	public int getAttributeKey(String attributeName) {
		int idx = 1; // start at 1
        for (Attribute att : attributes) {
            if (att.getName().equals(attributeName)) 
                return idx;
            idx++;
        }
        return -1;
    }	
}
