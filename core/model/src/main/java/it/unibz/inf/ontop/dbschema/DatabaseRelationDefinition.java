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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a database relation (either a table or a view)
 *
 * @author Roman Kontchakov
 *
 */

public class DatabaseRelationDefinition extends RelationDefinition {

	private UniqueConstraint primaryKey; // nullable
	private final List<UniqueConstraint> uniqueConstraints = new LinkedList<>();
	private final List<FunctionalDependency> otherFunctionalDependencies = new ArrayList<>();
	private final List<ForeignKeyConstraint> foreignKeys = new ArrayList<>();

	/**
	 * used only in DummyDBMetadataBuilder
	 *
	 * @param id
	 * @param builder
	 */
    public DatabaseRelationDefinition(RelationID id, AttributeListBuilder builder) {
		super(id, builder);
	}

	/**
	 * adds a unique constraint (a primary key or a unique constraint proper)
	 *
	 * @param uc
	 */

	public void addUniqueConstraint(UniqueConstraint uc) {
		if (uc.isPrimaryKey()) {
			if (primaryKey != null)
				throw new IllegalArgumentException("Duplicate PK " + primaryKey + " " + uc);
			primaryKey = uc;
		}
		uniqueConstraints.add(uc);
	}

	/**
	 * returns the list of unique constraints (including the primary key if present)
	 *
	 * @return
	 */
	@Override
	public ImmutableList<UniqueConstraint> getUniqueConstraints() {
		return ImmutableList.copyOf(uniqueConstraints);
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
	 * @return primary key
	 */
	@JsonIgnore
	@Override
	public Optional<UniqueConstraint> getPrimaryKey() {
		return Optional.ofNullable(primaryKey);
	}


	/**
	 * adds a foreign key constraints
	 *
	 * @param fk a foreign key
	 */

	public void addForeignKeyConstraint(ForeignKeyConstraint fk) {
		foreignKeys.add(fk);
	}

	/**
	 * returns the list of foreign key constraints
	 *
	 * @return list of foreign keys
	 */
	@JsonSerialize(contentUsing = ForeignKeyConstraint.ForeignKeyConstraintSerializer.class)
	@Override
	public ImmutableList<ForeignKeyConstraint> getForeignKeys() {
		return ImmutableList.copyOf(foreignKeys);
	}

	@Override
	public String toString() {
		return "CREATE TABLE " + getID() + " (\n   " +
				getAttributes().stream()
						.map(Attribute::toString)
						.collect(Collectors.joining(",\n   ")) +
				"\n)";
	}
}
