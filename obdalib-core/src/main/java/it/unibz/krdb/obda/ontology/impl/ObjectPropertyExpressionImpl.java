package it.unibz.krdb.obda.ontology.impl;

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

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.ObjectSomeValuesFrom;

/**
 * Represents ObjectPropertyExpression from the OWL 2 QL Specification
 * 
 * ObjectPropertyExpression := ObjectProperty | InverseObjectProperty
 * InverseObjectProperty := 'ObjectInverseOf' '(' ObjectProperty ')'
 * 
 * Support for owl:topObjectProperty and owl:bottomObjectProperty
 *     - the inverses of the two coincide with themselves (rule [R6])
 * 
 * @author Roman Kontchakov
 *
 */


public class ObjectPropertyExpressionImpl implements ObjectPropertyExpression {

	private static final long serialVersionUID = -2514037755762973974L;
	
	private final boolean isInverse;
	private final Predicate predicate;
	private final String string;
	private final ObjectPropertyExpressionImpl inverseProperty;
	
	private final ObjectSomeValuesFromImpl domain;
	
	private final boolean isTop, isBottom;

	public static final String owlTopObjectPropertyIRI = "http://www.w3.org/2002/07/owl#topObjectProperty";
	public static final String owlBottomObjectPropertyIRI = "http://www.w3.org/2002/07/owl#bottomObjectProperty";
	
	private static final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
	
	static final ObjectPropertyExpression owlTopObjectProperty = new ObjectPropertyExpressionImpl(owlTopObjectPropertyIRI); 
	static final ObjectPropertyExpression owlBottomObjectProperty = new ObjectPropertyExpressionImpl(owlBottomObjectPropertyIRI); 

	/**
	 * general constructor 
	 * 
	 * @param p
	 */
	
	ObjectPropertyExpressionImpl(String name) {
		this.predicate = ofac.getObjectPropertyPredicate(name);
		this.isInverse = false;
		this.string = name;
		this.isTop = name.equals(owlTopObjectPropertyIRI);
		this.isBottom = name.equals(owlBottomObjectPropertyIRI);
		if (isTop || isBottom) 
			this.inverseProperty = this;   // rule [R6] 
		else
			this.inverseProperty = new ObjectPropertyExpressionImpl(predicate, this);
		
		this.domain = new ObjectSomeValuesFromImpl(this);
	}
	
	/**
	 * special constructor for creating the inverse of an object property
	 *  (this constructor is never applied to the top and bottom properties)
	 * 
	 * @param p
	 * @param inverseProperty
	 */

	private ObjectPropertyExpressionImpl(Predicate p, ObjectPropertyExpressionImpl inverseProperty) {
		this.predicate = p;
		this.isInverse = true; // always inverted
		this.isTop = false; // cannot be the top property
		this.isBottom = false; // cannot be the bottom property
		this.inverseProperty = inverseProperty;
		// always inverted
		this.string = new StringBuilder().append(predicate.getName()).append("^-").toString();
		this.domain = new ObjectSomeValuesFromImpl(this);		
	}

	@Override
	public boolean isInverse() {
		return isInverse;
	}

	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	@Override
	public String getName() {
		return predicate.getName();
	}
	
	@Override
	public ObjectPropertyExpression getInverse() {
		return inverseProperty;
	}	


	@Override
	public ObjectSomeValuesFrom getDomain() {
		return domain;
	}

	@Override
	public ObjectSomeValuesFrom getRange() {
		return inverseProperty.domain;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ObjectPropertyExpressionImpl) {
			ObjectPropertyExpressionImpl other = (ObjectPropertyExpressionImpl) obj;
			return string.equals(other.string) && (isInverse == other.isInverse);
		}
		
		// the two types of properties share the same name space
		if (obj instanceof DataPropertyExpressionImpl) {
			DataPropertyExpressionImpl other = (DataPropertyExpressionImpl) obj;
			return (isInverse == false) && getName().equals(other.getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}
	
	@Override
	public String toString() {
		return string;
	}

	@Override
	public boolean isBottom() {
		return isBottom;
	}

	@Override
	public boolean isTop() {
		return isTop;
	}
}
