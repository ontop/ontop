package it.unibz.krdb.sql;

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

import java.util.*;

/**
 * Represents a complex sub-query created by the SQL parser (not a database view!)
 * 
 * @author Roman Kontchakov
*/

public class ParserViewDefinition extends RelationDefinition {

	private final List<Attribute> attributes = new ArrayList<>();
	private final Map<QualifiedAttributeID, Attribute> attributeMap = new HashMap<>();
	
	private final String statement;
	
	/**
	 * used only in DBMetadata
	 * 
	 * @param name
	 * @param statement
	 */
	
	ParserViewDefinition(RelationID name, String statement) {
		super(name);
		this.statement = statement;
	}

	/**
	 * adds a new attribute
	 * <p>
	 * (note that attributes may have fully qualified names because 
	 * attributes in the sub-query do not necessarily have aliases)
	 * 
	 * @param name
	 */
	public void addAttribute(QualifiedAttributeID name) {
		Attribute att = new Attribute(this, name, attributes.size() + 1, 0, null, true);
		Attribute prev = attributeMap.put(name, att);
		if (prev != null) 
			throw new IllegalArgumentException("Duplicate attribute names");
		
		attributes.add(att);
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
		Attribute attribute = attributes.get(index - 1);
		return attribute;
	}

	@Override
	public List<Attribute> getAttributes() {
		return Collections.unmodifiableList(attributes);
	}
	
	
	@Override
	public String toString() {
		StringBuilder bf = new StringBuilder();
		bf.append(getID());
		bf.append(" [");
		Joiner.on(", ").appendTo(bf, attributes);
		bf.append("]");
		bf.append(" (").append(statement).append(")");
		return bf.toString();
	}

}
