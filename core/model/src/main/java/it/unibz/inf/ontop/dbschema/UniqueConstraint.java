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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.impl.UniqueConstraintImpl;

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

public interface UniqueConstraint extends FunctionalDependency {

	/**
	 * return the name of the constraint
	 *
	 * @return name
	 */

	String getName();

	/**
	 * return true if it is a primary key and false otherwise
	 *
	 * @return true if it is a primary key constraint (false otherwise)
	 */

	boolean isPrimaryKey();

	/**
	 * return the list of attributes in the unique constraint
	 *
	 * @return list of attributes
	 */

	ImmutableList<Attribute> getAttributes();


	/**
	 * creates a UNIQUE constraint builder
	 *
	 * @param relation
	 * @param name
	 * @return
	 */

	static Builder builder(NamedRelationDefinition relation, String name) {
		return UniqueConstraintImpl.builder(relation, name);
	}

	/**
	 * creates a PRIMARY KEY  builder
	 *
	 * @param relation
	 * @param name
	 * @return
	 */

	static Builder primaryKeyBuilder(NamedRelationDefinition relation, String name) {
		return UniqueConstraintImpl.primaryKeyBuilder(relation, name);
	}


	static void primaryKeyOf(Attribute attribute) {
		NamedRelationDefinition relation = (NamedRelationDefinition)attribute.getRelation();
		primaryKeyBuilder(relation, "PK")
				.addDeterminant(attribute.getIndex()).build();
	}

	static void primaryKeyOf(Attribute attribute1, Attribute attribute2) {
		if (attribute1.getRelation() != attribute2.getRelation())
			throw new IllegalArgumentException();

		NamedRelationDefinition relation = (NamedRelationDefinition)attribute1.getRelation();

		primaryKeyBuilder(relation, "PK")
				.addDeterminant(attribute1.getIndex())
				.addDeterminant(attribute2.getIndex()).build();
	}
}
