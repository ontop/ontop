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

import com.google.common.collect.ImmutableList;

/**
 * Primary key or a unique constraint<br>
 * 
 * PRIMARY KEY (columnName (, columnName)*)<br>		
 * UNIQUE (columnName (, columnName)*)<br>	
 * 
 * (a form of equality-generating dependencies)
 * 
 * @author Roman Kontchakov
 *
 */

public class UniqueConstraint {

	public static final class Builder {
		private final ImmutableList.Builder<Attribute> builder = new ImmutableList.Builder<>();
		private final RelationDefinition relation;
		
		/**
		 * creates a UK builder 
		 * @param name of the UK constraint
		 * @param relation 
		 */
		
		public Builder(RelationDefinition relation) {
			this.relation = relation;
		}
		
		public Builder add(Attribute attribute) {
			if (relation != attribute.getRelation())
				throw new IllegalArgumentException("Unique Key requires the same table in all attributes: " + relation + " " + attribute);
			
			builder.add(attribute);
			return this;
		}
		
		public UniqueConstraint build(String name) {
			ImmutableList<Attribute> attributes = builder.build();
			if (attributes.isEmpty())
				return null;
			return new UniqueConstraint(name, builder.build());
		}
	}
	
	public static UniqueConstraint of(Attribute att) {
		UniqueConstraint.Builder builder = new UniqueConstraint.Builder(att.getRelation());
		builder.add(att);
		return builder.build("PK_" + att.getRelation().getName());
	}

	public static UniqueConstraint of(Attribute att, Attribute att2) {
		UniqueConstraint.Builder builder = new UniqueConstraint.Builder(att.getRelation());
		builder.add(att).add(att2);
		return builder.build("PK_" + att.getRelation().getName());
	}
	
	public static Builder builder(RelationDefinition relation) {
		return new Builder(relation);
	}
	
	private final String name;
	private final ImmutableList<Attribute> attributes;
	
	private UniqueConstraint(String name, ImmutableList<Attribute> attributes) {
		this.name = name;
		this.attributes = attributes;
	}
	
	public ImmutableList<Attribute> getAttributes() {
		return attributes;
	}
	
	public String getName() {
		return name;
	}
}
