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
import it.unibz.inf.ontop.model.atom.RelationPredicate;

import javax.annotation.Nullable;
import java.util.List;


/**
 * Basis of the representation for information on both<br>
 *   (a) relational tables and views<br>
 *   (b) and views created by the SQL parser for complex sub-queries
 *
 *
 * @author Roman Kontchakov
 *
 */

public abstract class RelationDefinition {

	private final RelationID id;

	/**
	 * Lazy
	 */
	@Nullable
	private RelationPredicate predicate;

	protected RelationDefinition(RelationID id) {
		this.id = id;
	}

	@JsonProperty("name")
	@JsonSerialize(using = RelationID.RelationIDSerializer.class)
	public RelationID getID() {
		return id;
	}

	public abstract Attribute getAttribute(int index);

	public abstract List<Attribute> getAttributes();

	/**
	 * Call it only after having completely assigned the attributes!
	 */
	@JsonIgnore
	public RelationPredicate getAtomPredicate() {
		if (predicate == null)
			predicate = new RelationPredicateImpl(this);
		return predicate;
	}

	public abstract ImmutableList<UniqueConstraint> getUniqueConstraints();

	public abstract ImmutableList<FunctionalDependency> getOtherFunctionalDependencies();

	public abstract UniqueConstraint getPrimaryKey();

	public abstract ImmutableList<ForeignKeyConstraint> getForeignKeys();
}
