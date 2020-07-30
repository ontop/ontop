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
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.type.DBTermType;


/**
 * Basis of the representation for information on both<br>
 *   (a) relational tables and views<br>
 *   (b) and views created by the SQL parser for complex sub-queries
 *
 * @author Roman Kontchakov
 */

public interface RelationDefinition {


	/**
	 * gets the attribute with the specified position
	 *
	 * @param index is position <em>starting at 1</em>
	 * @return attribute at the position
	 */

	Attribute getAttribute(int index);

	/**
	 * gets the attribute with the specified ID
	 *
	 * @param id
	 * @return
	 */

	Attribute getAttribute(QuotedID id) throws AttributeNotFoundException;

	/**
	 * the list of attributes
	 *
	 * @return list of attributes
	 */
	ImmutableList<Attribute> getAttributes();

	RelationPredicate getAtomPredicate();

	ImmutableList<UniqueConstraint> getUniqueConstraints();

	ImmutableList<FunctionalDependency> getOtherFunctionalDependencies();

	ImmutableList<ForeignKeyConstraint> getForeignKeys();


	interface AttributeListBuilder {

		AttributeListBuilder addAttribute(QuotedID id, DBTermType termType, String typeName, boolean isNullable);

		AttributeListBuilder addAttribute(QuotedID id, DBTermType termType, boolean isNullable);

		ImmutableList<Attribute> build(RelationDefinition relation);
	}
}
