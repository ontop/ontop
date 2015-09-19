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
		private final RelationDefinition relation, referencedRelation;
		
		/**
		 * creates a FOREIGN KEY builder 
		 * 
		 * @param relation 
		 * @param referencedRelation
		 */
		
		public Builder(RelationDefinition relation, RelationDefinition referencedRelation) {
			this.relation = relation;
			this.referencedRelation = referencedRelation;
		}
		
		/**
		 * adds a pair (attribute, referenced attribute) to the FK constraint
		 * 
		 * @param attribute
		 * @param referencedAttribute
		 * @return
		 */
		
		public Builder add(Attribute attribute, Attribute referencedAttribute) {
			if (relation != attribute.getRelation())
				throw new IllegalArgumentException("Foreign Key requires the same table in all attributes: " + relation + " -> " + referencedRelation);
			
			if (referencedRelation != referencedAttribute.getRelation())
				throw new IllegalArgumentException("Foreign Key requires the same table in all referenced attributes: "  + relation + " -> " + referencedRelation);
			
			builder.add(new Component(attribute, referencedAttribute));
			return this;
		}

		/**
		 * builds a FOREIGN KEY constraint
		 * 
		 * @param name
		 * @return null if the list of components is empty
		 */
		
		public ForeignKeyConstraint build(String name) {
			ImmutableList<Component> components = builder.build();
			if (components.isEmpty())
				return null;
			
			return new ForeignKeyConstraint(name, components);
		}
	}
	
	public static ForeignKeyConstraint of(String name, Attribute attribute, Attribute reference) {
		return new Builder(attribute.getRelation(), reference.getRelation())
						.add(attribute, reference).build(name);
	}
	
	private final String name;
	private final ImmutableList<Component> components;
	private final RelationDefinition referencedRelation;
	
	/**
	 * private constructor (use Builder instead)
	 * 
	 * @param name
	 * @param components
	 */
	
	private ForeignKeyConstraint(String name, ImmutableList<Component> components) {
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
