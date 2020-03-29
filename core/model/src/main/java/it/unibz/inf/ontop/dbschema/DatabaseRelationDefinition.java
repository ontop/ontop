package it.unibz.inf.ontop.dbschema;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.*;

/**
 * Represents a database relation (either a table or a view)
 *
 * @author Roman Kontchakov
 *
 */

public class DatabaseRelationDefinition extends RelationDefinition {

	private final List<Attribute> attributes = new ArrayList<>();
	private final Map<QuotedID, Attribute> attributeMap = new HashMap<>();

	private final List<UniqueConstraint> ucs = new LinkedList<>();
	private final List<ForeignKeyConstraint> fks = new LinkedList<>();
	private final List<FunctionalDependency> otherFunctionalDependencies = new ArrayList<>();
	private UniqueConstraint pk;


	/**
	 * used only in DBMetadata
	 *
	 * @param name
	 */
	protected DatabaseRelationDefinition(RelationID name) {
		super(name);
	}

	/**
	 * creates a new attribute
	 *
	 * @param id
	 * @param typeName
	 * @param termType
	 * @param canNull
	 */
	public Attribute addAttribute(QuotedID id, String typeName, DBTermType termType, boolean canNull) {
		Attribute att = new Attribute(this, id, attributes.size() + 1, typeName, termType, canNull);

		//check for duplicate names (put returns the previous value)
		Attribute prev = attributeMap.put(id, att);
		if (prev != null)
			throw new IllegalArgumentException("Duplicate attribute names");

		attributes.add(att);
		return att;
	}

	/**
	 * return an attribute with the specified ID
	 *
	 * @param attributeId
	 * @return
	 */

	public Attribute getAttribute(QuotedID attributeId) {
		return attributeMap.get(attributeId);
	}

	/**
	 * gets attribute with the specified position
	 *
	 * @param index is position <em>starting at 1</em>
	 * @return attribute at the position
	 */
	@Override
	public Attribute getAttribute(int index) {
		Attribute attribute = attributes.get(index - 1);
		return attribute;
	}

	/**
	 * returns the list of attributes
	 *
	 * @return list of attributes
	 */
	@JsonProperty("columns")
	@Override
	public List<Attribute> getAttributes() {
		return Collections.unmodifiableList(attributes);
	}

	/**
	 * adds a unique constraint (a primary key or a unique constraint proper)
	 *
	 * @param uc
	 */

	public void addUniqueConstraint(UniqueConstraint uc) {
		if (uc.isPrimaryKey()) {
			if (pk != null)
				throw new IllegalArgumentException("Duplicate PK " + pk + " " + uc);
			pk = uc;
		}
		else {
			if (pk != null)
				if (uc.getAttributes().equals(pk.getAttributes()))
					// ignore the unique index created for the primary key
					return;
		}
		ucs.add(uc);
	}

	/**
	 * returns the list of unique constraints (including the primary key if present)
	 *
	 * @return
	 */
	@Override
	public ImmutableList<UniqueConstraint> getUniqueConstraints() {
		return ImmutableList.copyOf(ucs);
	}

	public void addFunctionalDependency(FunctionalDependency constraint) {
		if (constraint instanceof UniqueConstraint)
			addUniqueConstraint((UniqueConstraint) constraint);
		else
			otherFunctionalDependencies.add(constraint);
	}

	@Override
	public ImmutableList<FunctionalDependency> getOtherFunctionalDependencies() {
		return ImmutableList.copyOf(otherFunctionalDependencies);
	}

	/**
	 * return primary key (if present) or null (otherwise)
	 *
	 * @return
	 */
	@JsonIgnore
	@Override
	public UniqueConstraint getPrimaryKey() {
		return pk;
	}


	/**
	 * adds a foreign key constraints
	 *
	 * @param fk a foreign key
	 */

	public void addForeignKeyConstraint(ForeignKeyConstraint fk) {
		fks.add(fk);
	}

	/**
	 * returns the list of foreign key constraints
	 *
	 * @return list of foreign keys
	 */
	@JsonSerialize(contentUsing = ForeignKeyConstraint.ForeignKeyConstraintSerializer.class)
	@Override
	public ImmutableList<ForeignKeyConstraint> getForeignKeys() {
		return ImmutableList.copyOf(fks);
	}


	@Override
	public String toString() {
		StringBuilder bf = new StringBuilder();
		bf.append("CREATE TABLE ").append(getID()).append(" (\n   ");
		Joiner.on(",\n   ").appendTo(bf, attributes);
		bf.append("\n)");
		return bf.toString();
	}

}
