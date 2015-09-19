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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Basis of the representation for information on relational tables and views
 * (attributes and integrity constraints: primary keys, unique keys and foreign keys)
 * 
 * @author Roman Kontchakov
 *
 */

public abstract class DatabaseRelationDefinition {

	private final String givenName;

	private final String catalogName;
	private final String schemaName;
	private final String tableName;
	
	private final List<Attribute> attributes = new LinkedList<>();

	private UniqueConstraint pk;
	private final List<UniqueConstraint> ucs = new LinkedList<>();
	private final List<ForeignKeyConstraint> fks = new LinkedList<>();
		
	protected DatabaseRelationDefinition(String catalogName, String schemaName, String tableName, String name) {
		this.catalogName = catalogName;
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.givenName = name;
	}

	public String getName() {
		return givenName;
	}

	public String getCatalog() {
		return catalogName;
	}
	
	public String getSchema() {
		return schemaName;
	}
	
	public String getTableName() {
		return tableName;
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
	
	public void addForeignKeyConstraint(ForeignKeyConstraint fk) {
		fks.add(fk);
	}
	
	public ImmutableList<ForeignKeyConstraint> getForeignKeys() {
		return ImmutableList.copyOf(fks);
	}
	
	public void addAttribute(String name, int type, String typeName, boolean canNull) {
		attributes.add(new Attribute(this, name, type, typeName, canNull));
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
	
	public Attribute getAttribute(String attributeName) {
		int pos = getAttributeKey(attributeName);
		return getAttribute(pos);
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
