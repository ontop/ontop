package it.unibz.inf.ontop.spec.sqlparser;

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
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.AbstractRelationDefinition;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.util.stream.Collectors;

/**
 * Represents a complex sub-query created by the SQL parser (not a database view!)
 *
 * @author Roman Kontchakov
*/

public class ParserViewDefinition extends AbstractRelationDefinition {
	
	public ParserViewDefinition(ImmutableList<QuotedID> attrs, String statement, DBTypeFactory dbTypeFactory) {
		this(attributeListBuilder(attrs, dbTypeFactory), statement);
	}

	public ParserViewDefinition(AttributeListBuilder attributeListBuilder, String statement) {
		super("(" + statement + ")", attributeListBuilder);
	}

	private static AttributeListBuilder attributeListBuilder(ImmutableList<QuotedID> attrs, DBTypeFactory dbTypeFactory) {
		AttributeListBuilder builder = attributeListBuilder();
		for (QuotedID id : attrs) {
			builder.addAttribute(id, dbTypeFactory.getAbstractRootDBType(), null, true);
		}
		return builder;
	}


	@Override
	public ImmutableList<UniqueConstraint> getUniqueConstraints() {
		return ImmutableList.of();
	}

	@Override
	public ImmutableList<FunctionalDependency> getOtherFunctionalDependencies() {
		return ImmutableList.of();
	}

	@Override
	public ImmutableList<ForeignKeyConstraint> getForeignKeys() {
		return ImmutableList.of();
	}

	@Override
	public String toString() {
		return getAtomPredicate().getName() + "[" + getAttributes().stream()
				.map(Attribute::toString)
				.collect(Collectors.joining(", ")) + "]";
	}
}
