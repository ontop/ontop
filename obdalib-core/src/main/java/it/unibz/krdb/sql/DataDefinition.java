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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public abstract class DataDefinition implements Serializable {

	private static final long serialVersionUID = 212770563440334334L;

	private final String name;
	private final List<Attribute> attributes = new LinkedList<>();

	private UniqueConstraint pk;
	private LinkedList<UniqueConstraint> ucs = new LinkedList<>();
		
	protected DataDefinition(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	
	public void setPrimaryKey(ImmutableList<Attribute> attributes) {
		pk = new UniqueConstraint(attributes);
	}
	
	public UniqueConstraint getPrimaryKey() {
		return pk;
	}
	
	public void addUniqueConstraint(ImmutableList<Attribute> attributes) {
		UniqueConstraint uc = new UniqueConstraint(attributes);
		ucs.add(uc);
	}
	
	public ImmutableList<UniqueConstraint> getUniqueConstraints() {
		return ImmutableList.copyOf(ucs);
	}
	
	public void addAttribute(Attribute value) {
		attributes.add(value);
	}

	// TODO: remove from ImplicitDBConstraints
	@Deprecated
	public void setAttribute(int pos, Attribute value) {
		// indexes start at 1
		attributes.set(pos - 1, value);
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
