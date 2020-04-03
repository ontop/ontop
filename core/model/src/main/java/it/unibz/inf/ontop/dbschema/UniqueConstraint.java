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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.Collectors;

/**
 * Primary key or a unique constraint
 *
 * PRIMARY KEY (columnName (, columnName)*)
 * UNIQUE (columnName (, columnName)*)
 *
 * (a form of equality-generating dependencies)
 *
 * @author Roman Kontchakov
 *
 */

public class UniqueConstraint implements FunctionalDependency {

	public static final class BuilderImpl implements FunctionalDependency.Builder {
		private final ImmutableList.Builder<Attribute> builder = new ImmutableList.Builder<>();
		private final DatabaseRelationDefinition relation;
		private final String name;
		private boolean isPK;

		private BuilderImpl(DatabaseRelationDefinition relation, String name, boolean isPK) {
			this.relation = relation;
			this.name = name;
			this.isPK = isPK;
		}

		@Override
		public BuilderImpl addDeterminant(Attribute attribute) {
			if (relation != attribute.getRelation())
				throw new IllegalArgumentException("UC requires the same table in all attributes: " + relation + " " + attribute);

			builder.add(attribute);
			return this;
		}

		@Override
		public BuilderImpl addDependent(Attribute dependent) {
			throw new IllegalArgumentException("No dependents");
		}

		@Override
		public UniqueConstraint build() {
			ImmutableList<Attribute> attributes = builder.build();
			if (attributes.isEmpty())
				throw new IllegalArgumentException("UC cannot have no attributes");
			return new UniqueConstraint(name, isPK, attributes);
		}
	}

	public static UniqueConstraint primaryKeyOf(Attribute att) {
		BuilderImpl builder = primaryKeyBuilder((DatabaseRelationDefinition)att.getRelation(),
				"PK_" + att.getRelation().getID().getTableName());
		return builder.addDeterminant(att).build();
	}

	public static UniqueConstraint primaryKeyOf(Attribute att1, Attribute att2) {
		BuilderImpl builder = primaryKeyBuilder((DatabaseRelationDefinition)att1.getRelation(),
				"PK_" + att1.getRelation().getID().getTableName());
		return builder.addDeterminant(att1).addDeterminant(att2).build();
	}

	/**
	 * creates a UNIQUE constraint builder
	 *
	 * @param relation
	 * @param name
	 * @return
	 */

	public static BuilderImpl builder(DatabaseRelationDefinition relation, String name) {
		return new BuilderImpl(relation, name, false);
	}

	/**
	 * creates a PRIMARY KEY  builder
	 *
	 * @param relation
	 * @param name
	 * @return
	 */

	public static BuilderImpl primaryKeyBuilder(DatabaseRelationDefinition relation, String name) {
		return new BuilderImpl(relation, name, true);
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
		return "ALTER TABLE " + attributes.get(0).getRelation().getID() +
				" ADD CONSTRAINT " + name + (isPK ? " PRIMARY KEY " : " UNIQUE ") +
				"(" +
				attributes.stream()
						.map(Attribute::getID)
						.map(QuotedID::toString)
						.collect(Collectors.joining(", ")) +
				")";
	}
}
