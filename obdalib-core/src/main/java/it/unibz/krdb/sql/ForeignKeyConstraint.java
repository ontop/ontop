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
 * Foreign Key constraints<br>
 * 
 * FOREIGN KEY (columnName (, columnName)*)  
 * 			REFERENCES refTableName	(refColumnName (, refColumnName)*)<br>
 * 
 * (a particular case of linear tuple-generating dependencies<br>
 * \forall x (\exists y_1 R_1(x,y_1) \to \exists y_2 R_2(x,y_2))<br>
 * where x, y_1 and y_2 are *tuples* of variables)
 * 
 * @author Roman Kontchakov
 *
 */

public class ForeignKeyConstraint {

	public static final class Component {
		private final Attribute attribute, reference;
		
		private Component(Attribute attribute, Attribute reference) {
			this.attribute = attribute;
			this.reference = reference;
		}
		
		public Attribute getAttribute() {
			return attribute;
		}
		
		public Attribute getReference() {
			return reference;
		}
	}
	
	public static final class Builder {
		private final ImmutableList.Builder<Component> builder = new ImmutableList.Builder<>();
		private final String name;
		private final RelationDefinition relation, referencedRelation;
		
		/**
		 * creates a FK builder 
		 * @param name of the FK constraint
		 * @param relation 
		 * @param referencedRelation
		 */
		
		public Builder(String name, RelationDefinition relation, RelationDefinition referencedRelation) {
			this.name = name;
			this.relation = relation;
			this.referencedRelation = referencedRelation;
		}
		
		public Builder add(Attribute attribute, Attribute reference) {
			if (relation != attribute.getRelation())
				throw new IllegalArgumentException("Foreign Key requires the same table in all attributes: " + name);
			
			if (referencedRelation != reference.getRelation())
				throw new IllegalArgumentException("Foreign Key requires the same table in all referenced attributes: " + name);
			
			builder.add(new Component(attribute, reference));
			return this;
		}
		
		public ForeignKeyConstraint build() {
			return new ForeignKeyConstraint(name, builder.build());
		}
	}
	
	public static ForeignKeyConstraint of(String name, Attribute attribute, Attribute reference) {
		return new Builder(name,attribute.getRelation(), reference.getRelation())
						.add(attribute, reference).build();
	}
	
	private final ImmutableList<Component> components;
	private final String name;
	private final RelationDefinition referencedRelation;
	
	private ForeignKeyConstraint(String name, ImmutableList<Component> components) {
		if (components.isEmpty())
			throw new IllegalArgumentException("Empty list of components is not allowed in Foreign Key constraints");
		
		this.name = name;
		this.components = components;
		this.referencedRelation = components.get(0).getReference().getRelation();
	}
	
	public String getName() {
		return name;
	}
	
	public ImmutableList<Component> getComponents() {
		return components;
	}
	
	public RelationDefinition getReferencedRelation() {
		return referencedRelation;
	}
}
