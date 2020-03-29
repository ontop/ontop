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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.util.*;

/**
 * Represents a complex sub-query created by the SQL parser (not a database view!)
 *
 * @author Roman Kontchakov
*/

public class ParserViewDefinition extends RelationDefinition {

	private final ImmutableList<Attribute> attributes;

	private final String statement;
	
	/**
	 *  @param name
	 * @param statement
	 */
	
	public ParserViewDefinition(RelationID name, ImmutableList<QuotedID> attrs, String statement,
								DBTypeFactory dbTypeFactory) {
		super(name);
		this.statement = statement;

		ImmutableList.Builder<Attribute> attributeBuilder = ImmutableList.builder();
		int c = 1;
		for (QuotedID id : attrs) {
			// TODO: infer types?
			Attribute att = new Attribute(this,
					id, c, null, dbTypeFactory.getAbstractRootDBType(), true);
			c++;
			attributeBuilder.add(att);
		}
		this.attributes = attributeBuilder.build();
	}

	/**
	 * returns the SQL definition of the sub-query
	 *  
	 * @return
	 */
	
	public String getStatement() {
		return statement;
	}

	@Override
	public Attribute getAttribute(int index) {
		// positions start at 1
		return attributes.get(index - 1);
	}

	@Override
	public List<Attribute> getAttributes() { return attributes; }

	@Override
	public ImmutableList<UniqueConstraint> getUniqueConstraints() {
		return ImmutableList.of();
	}

	@Override
	public ImmutableList<FunctionalDependency> getOtherFunctionalDependencies() {
		return ImmutableList.of();
	}

	@Override
	public UniqueConstraint getPrimaryKey() {
		return null;
	}

	@Override
	public ImmutableList<ForeignKeyConstraint> getForeignKeys() {
		return ImmutableList.of();
	}


	@Override
	public String toString() {
		StringBuilder bf = new StringBuilder();
		bf.append(getID()).append(" [");
		Joiner.on(", ").appendTo(bf, attributes);
		bf.append("]").append(" (").append(statement).append(")");
		return bf.toString();
	}

}
