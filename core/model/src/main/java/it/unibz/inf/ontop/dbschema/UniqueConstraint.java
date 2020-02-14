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
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.ArrayList;
import java.util.List;

/**
 * Primary key or a unique constraint<br>
 *
 * PRIMARY KEY (columnName (, columnName)*)<br>
 * UNIQUE (columnName (, columnName)*)<br>
 *
 * (a form of equality-generating dependencies)
 *
 * @author Roman Kontchakov
 *
 */

public class UniqueConstraint implements FunctionalDependency {

	public static final class Builder {
		private final ImmutableList.Builder<Attribute> builder = new ImmutableList.Builder<>();
		private final DatabaseRelationDefinition relation;

		/**
		 * creates a UNIQUE constraint builder
		 *
		 * @param relation
		 */

		public Builder(DatabaseRelationDefinition relation) {
			this.relation = relation;
		}

		/**
		 * adds an attribute to the UNIQUE constraint
		 *
		 * @param attribute
		 * @return
		 */

		public Builder add(Attribute attribute) {
			if (relation != attribute.getRelation())
				throw new IllegalArgumentException("Unique Key requires the same table in all attributes: " + relation + " " + attribute);

			builder.add(attribute);
			return this;
		}

		/**
		 * builds a UNIQUE constraint (this includes PRIMARY KEY)
		 *
		 * @param name
		 * @return null if the list of attributes is empty
		 */

		public UniqueConstraint build(String name, boolean isPK) {
			ImmutableList<Attribute> attributes = builder.build();
			if (attributes.isEmpty())
				return null;
			return new UniqueConstraint(name, isPK, builder.build());
		}
	}

	public static UniqueConstraint primaryKeyOf(Attribute att) {
		UniqueConstraint.Builder builder = new UniqueConstraint.Builder((DatabaseRelationDefinition)att.getRelation());
		return builder.add(att).build("PK_" + att.getRelation().getID().getTableName(), true);
	}

	public static UniqueConstraint primaryKeyOf(Attribute att, Attribute att2) {
		UniqueConstraint.Builder builder = new UniqueConstraint.Builder((DatabaseRelationDefinition)att.getRelation());
		return builder.add(att).add(att2).build("PK_" + att.getRelation().getID().getTableName(), true);
	}

	/**
	 * creates a UNIQUE constraint builder (which is also used for a PRIMARY KET builder)
	 *
	 * @param relation
	 * @return
	 */

	public static Builder builder(DatabaseRelationDefinition relation) {
		return new Builder(relation);
	}

	private final String name;
	private final ImmutableList<Attribute> attributes;
	private final boolean isPK; // primary key

	/**
	 * private constructor (use Builder instead)
	 *
	 * @param name
	 * @param attributes
	 */

	private UniqueConstraint(String name, boolean isPK, ImmutableList<Attribute> attributes) {
		this.name = name;
		this.isPK = isPK;
		this.attributes = attributes;
	}

	/**
	 * return the name of the constraint
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * return the database relation for the unique constraint
	 *
	 * @return
	 */
	@JsonIgnore
	public DatabaseRelationDefinition getRelation() {
		return (DatabaseRelationDefinition)attributes.get(0).getRelation();
	}

	/**
	 * return true if it is a primary key and false otherwise
	 *
	 * @return true if it is a primary key constraint (false otherwise)
	 */
	@JsonProperty("isPrimaryKey")
	public boolean isPrimaryKey() {
		return isPK;
	}

	/**
	 * return the list of attributes in the unique constraint
	 *
	 * @return list of attributes
	 */
	@JsonProperty("determinants")
	@JsonSerialize(contentUsing = Attribute.AttributeSerializer.class)
	public ImmutableList<Attribute> getAttributes() {
		return attributes;
	}

	@JsonIgnore
	@Override
	public ImmutableSet<Attribute> getDeterminants() {
		return ImmutableSet.copyOf(attributes);
	}

	@JsonIgnore
	@Override
	public ImmutableSet<Attribute> getDependents() {
		return getRelation().getAttributes().stream()
				.filter(a -> !attributes.contains(a))
				.collect(ImmutableCollectors.toSet());
	}

	@Override
	public String toString() {
		List<String> columns = new ArrayList<>(attributes.size());
		for (Attribute c : attributes)
			columns.add(c.getID().toString());

		StringBuilder bf = new StringBuilder();
		bf.append("ALTER TABLE ").append(attributes.get(0).getRelation().getID())
			.append(" ADD CONSTRAINT ").append(name).append(isPK ? " PRIMARY KEY " : " UNIQUE ")
			.append("(");
		Joiner.on(", ").appendTo(bf, columns);
		bf.append(")");
		return bf.toString();
	}
}
