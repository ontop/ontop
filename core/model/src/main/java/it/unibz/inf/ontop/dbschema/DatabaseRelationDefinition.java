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


import com.google.common.collect.ImmutableSet;

import java.util.Optional;

/**
 * Represents a database relation (either a table or a view)
 *
 * @author Roman Kontchakov
 *
 */

public interface DatabaseRelationDefinition extends RelationDefinition {

	RelationID getID();

	ImmutableSet<RelationID> getAllIDs();

	Optional<UniqueConstraint> getPrimaryKey();


	void addFunctionalDependency(FunctionalDependency constraint);

	void addForeignKeyConstraint(ForeignKeyConstraint fk);
}
