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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

/**
 * Basis of the representation for information on relational tables and views
 * (attributes and integrity constraints: primary keys, unique keys and foreign keys)
 * 
 * @author Roman Kontchakov
 *
 */

public abstract class RelationDefinition {

	private final RelationID name;
	
	private final List<Attribute> attributes = new ArrayList<>();
	private final Map<QuotedID, Attribute> attributeMap = new HashMap<>();

	private UniqueConstraint pk;
	private final List<UniqueConstraint> ucs = new LinkedList<>();
	private final List<ForeignKeyConstraint> fks = new LinkedList<>();
		
	protected RelationDefinition(RelationID name) {
		this.name = name;
	}

	public RelationID getName() {
		return name;
	}
	
	public void setPrimaryKey(UniqueConstraint uc) {
		pk = uc;
	}
	
	public UniqueConstraint getPrimaryKey() {
		return pk;
	}
	
	public void addUniqueConstraint(UniqueConstraint uc) {
		ucs.add(uc);
	}
	
	public ImmutableList<UniqueConstraint> getUniqueConstraints() {
		return ImmutableList.copyOf(ucs);
	}
	
	public void addForeignKeyConstraint(ForeignKeyConstraint fk) {
		fks.add(fk);
	}
	
	public ImmutableList<ForeignKeyConstraint> getForeignKeys() {
		return ImmutableList.copyOf(fks);
	}
	
	public void addAttribute(QuotedID name, int type, String typeName, boolean canNull) {
		Attribute att = new Attribute(this, attributes.size() + 1, name, type, typeName, canNull);
		Attribute prev = attributeMap.put(att.getName(), att);
		if (prev != null) 
			throw new IllegalArgumentException("Duplicate attribute names");
		
		attributes.add(att);
	}

	/**
	 * gets attribute with the specified position
	 * 
	 * @param index is position <em>staring at 1</em>
	 * @return attribute at the position
	 */
	public Attribute getAttribute(int index) {
		// positions start at 1
		Attribute attribute = attributes.get(index - 1);
		return attribute;
	}

	public List<Attribute> getAttributes() {
		return Collections.unmodifiableList(attributes);
	}
	
	public Attribute getAttribute(QuotedID attributeName) {
		return attributeMap.get(attributeName);
	}
}
