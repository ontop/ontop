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

import java.util.Objects;
import java.util.Optional;
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

	public static class UniqueConstraintBuilder implements FunctionalDependency.Builder {
		protected final ImmutableList.Builder<Attribute> builder = ImmutableList.builder();
		protected final DatabaseRelationDefinition relation;
		protected final String name;

		private UniqueConstraintBuilder(DatabaseRelationDefinition relation, String name) {
			this.relation = relation;
			this.name = name;
		}

		@Override
		public Builder addDeterminant(int determinantIndex) {
			builder.add(relation.getAttribute(determinantIndex));
			return this;
		}

		@Override
		public Builder addDeterminant(QuotedID determinantId) throws RelationDefinition.AttributeNotFoundException {
			builder.add(relation.getAttribute(determinantId));
			return this;
		}

		@Override
		public Builder addDependent(int dependentIndex) {
			throw new IllegalArgumentException("No dependents");
		}

		@Override
		public Builder addDependent(QuotedID dependentId) {
			throw new IllegalArgumentException("No dependents");
		}

		@Override
		public void build() {
			ImmutableList<Attribute> attributes = builder.build();
			if (attributes.isEmpty())
				throw new IllegalArgumentException("UC cannot have no attributes");

			Optional<UniqueConstraint> pk = relation.getPrimaryKey();
			if (pk.isPresent() && pk.get().getAttributes().equals(attributes))
				return; // ignore a unique constraint with the same attributes as the primary key

			relation.addUniqueConstraint(new UniqueConstraint(name, false, attributes));
		}
	}

	public static class PrimaryKeyBuilder extends UniqueConstraintBuilder {

		private PrimaryKeyBuilder(DatabaseRelationDefinition relation, String name) {
			super(relation, name);
		}

		@Override
		public Builder addDeterminant(int determinantIndex) {
			Attribute attribute = relation.getAttribute(determinantIndex);
			if (attribute.isNullable())
				throw new IllegalArgumentException("Nullable attribute " + attribute + " cannot be in a PK");

			builder.add(attribute);
			return this;
		}

		@Override
		public Builder addDeterminant(QuotedID determinantId) throws RelationDefinition.AttributeNotFoundException {
			Attribute attribute = relation.getAttribute(determinantId);
			if (attribute.isNullable())
				throw new IllegalArgumentException("Nullable attribute " + attribute + " cannot be in a PK");

			builder.add(attribute);
			return this;
		}

		@Override
		public void build() {
			ImmutableList<Attribute> attributes = builder.build();
			if (attributes.isEmpty())
				throw new IllegalArgumentException("PK cannot have no attributes");

			relation.addUniqueConstraint(new UniqueConstraint(name, true, attributes));
		}
	}


	public static void primaryKeyOf(Attribute attribute) {
		primaryKeyBuilder((DatabaseRelationDefinition)attribute.getRelation(), getRelationPrimaryKeyName(attribute.getRelation()))
				.addDeterminant(attribute.getIndex()).build();
	}

	public static void primaryKeyOf(Attribute attribute1, Attribute attribute2) {
		if (attribute1.getRelation() != attribute2.getRelation())
			throw new IllegalArgumentException();

		primaryKeyBuilder((DatabaseRelationDefinition)attribute1.getRelation(), getRelationPrimaryKeyName(attribute1.getRelation()))
				.addDeterminant(attribute1.getIndex())
				.addDeterminant(attribute1.getIndex()).build();
	}

	private static String getRelationPrimaryKeyName(RelationDefinition relation) { return "PK_" + relation.getID().getTableName(); }

	/**
	 * creates a UNIQUE constraint builder
	 *
	 * @param relation
	 * @param name
	 * @return
	 */

	public static Builder builder(DatabaseRelationDefinition relation, String name) {
		return new UniqueConstraintBuilder(relation, name);
	}

	/**
	 * creates a PRIMARY KEY  builder
	 *
	 * @param relation
	 * @param name
	 * @return
	 */

	public static Builder primaryKeyBuilder(DatabaseRelationDefinition relation, String name) {
		return new PrimaryKeyBuilder(relation, name);
	}

	private final String name;
	private final ImmutableList<Attribute> attributes;
	private final boolean isPrimaryKey;

	/**
	 * private constructor (use Builder instead)
	 *
	 * @param name
	 * @param attributes
	 */

	private UniqueConstraint(String name, boolean isPrimaryKey, ImmutableList<Attribute> attributes) {
		this.name = name;
		this.isPrimaryKey = isPrimaryKey;
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
	 * return true if it is a primary key and false otherwise
	 *
	 * @return true if it is a primary key constraint (false otherwise)
	 */

	@JsonProperty("isPrimaryKey")
	public boolean isPrimaryKey() {
		return isPrimaryKey;
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
		return attributes.get(0).getRelation().getAttributes().stream()
				.filter(a -> !attributes.contains(a))
				.collect(ImmutableCollectors.toSet());
	}

	@Override
	public String toString() {
		return "ALTER TABLE " + attributes.get(0).getRelation().getID() +
				" ADD CONSTRAINT " + name + (isPrimaryKey ? " PRIMARY KEY " : " UNIQUE ") +
				"(" +
				attributes.stream()
						.map(Attribute::getID)
						.map(QuotedID::toString)
						.collect(Collectors.joining(", ")) +
				")";
	}
}
