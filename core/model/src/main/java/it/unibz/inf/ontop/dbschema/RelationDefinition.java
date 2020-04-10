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
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.impl.RelationIDImpl;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.atom.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.function.Function;


/**
 * Basis of the representation for information on both<br>
 *   (a) relational tables and views<br>
 *   (b) and views created by the SQL parser for complex sub-queries
 *
 * @author Roman Kontchakov
 */

public abstract class RelationDefinition {

	private final RelationID id;
	private final ImmutableList<Attribute> attributes;
	private final ImmutableMap<QuotedID, Attribute> map;
	private final RelationPredicate predicate;

	protected RelationDefinition(AttributeListBuilder builder) {
		this.id = builder.relationID;
		this.attributes = builder.build(this);
		this.map = attributes.stream()
				.collect(ImmutableCollectors.toMap(Attribute::getID, Function.identity()));
		this.predicate = new RelationPredicateImpl();
	}

	@JsonProperty("name")
	@JsonSerialize(using = RelationIDImpl.RelationIDSerializer.class)
	public RelationID getID() {
		return id;
	}

	/**
	 * gets the attribute with the specified position
	 *
	 * @param index is position <em>starting at 1</em>
	 * @return attribute at the position
	 */

	public Attribute getAttribute(int index) { return attributes.get(index - 1); }

	/**
	 * gets the attribute with the specified ID
	 *
	 * @param id
	 * @return
	 */

	public Attribute getAttribute(QuotedID id) {
		Attribute attribute = map.get(id);
		if (attribute == null)
			throw new AttributeNotFoundException(id);

		return attribute;
	}

	/**
	 * returns the list of attributes
	 *
	 * @return list of attributes
	 */
	@JsonProperty("columns")
	public ImmutableList<Attribute> getAttributes() { return attributes; }

	/**
	 * Call it only after having completely assigned the attributes!
	 */
	@JsonIgnore
	public RelationPredicate getAtomPredicate() { return predicate;  }

	public abstract ImmutableList<UniqueConstraint> getUniqueConstraints();

	public abstract ImmutableList<FunctionalDependency> getOtherFunctionalDependencies();

	public abstract Optional<UniqueConstraint> getPrimaryKey();

	public abstract ImmutableList<ForeignKeyConstraint> getForeignKeys();

	public class AttributeNotFoundException extends RuntimeException {
		private final QuotedID attributeId;

		public AttributeNotFoundException(QuotedID attributeId) {
			this.attributeId = attributeId;
		}

		public QuotedID getAttributeID() { return attributeId; }

		public RelationDefinition getRelation() { return RelationDefinition.this; }
	}


	private class RelationPredicateImpl extends AtomPredicateImpl implements RelationPredicate {

		RelationPredicateImpl() {
			super(id.getSQLRendering(),
					attributes.stream()
							.map(Attribute::getTermType)
							.collect(ImmutableCollectors.toList()));
		}

		@Override
		public RelationDefinition getRelationDefinition() {
			return RelationDefinition.this;
		}
	}


	private static class AttributeInfo {
		private final QuotedID id;
		private final int index;
		private final DBTermType termType;
		private final String typeName;
		private final boolean isNullable;

		AttributeInfo(QuotedID id, int index, DBTermType termType, String typeName, boolean isNullable) {
			this.id = id;
			this.index = index;
			this.termType = termType;
			this.typeName = typeName;
			this.isNullable = isNullable;
		}
	}

	public static class AttributeListBuilder {
		private final List<AttributeInfo> list = new ArrayList<>();
		private final RelationID relationID;

		public AttributeListBuilder(RelationID relationID) {
			this.relationID = relationID;
		}

		public RelationID getRelationID() {
			return relationID;
		}

		public AttributeListBuilder addAttribute(QuotedID id, DBTermType termType, String typeName, boolean isNullable) {
			list.add(new AttributeInfo(id, list.size() + 1, termType, typeName, isNullable));
			return this;
		}

		public AttributeListBuilder addAttribute(QuotedID id, DBTermType termType, boolean isNullable) {
			list.add(new AttributeInfo(id, list.size() + 1, termType, termType.getName(), isNullable));
			return this;
		}

		public ImmutableList<Attribute> build(RelationDefinition relation) {
			return list.stream()
					.map(a -> new Attribute(relation, a.id, a.index, a.typeName, a.termType, a.isNullable))
					.collect(ImmutableCollectors.toList());
		}
	}
}
