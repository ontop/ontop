package org.semanticweb.ontop.ontology.impl;

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

import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.ontology.DataPropertyExpression;
import org.semanticweb.ontop.ontology.ObjectPropertyExpression;
import org.semanticweb.ontop.ontology.ObjectSomeValuesFrom;

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
	
	static final ObjectPropertyExpression owlTopObjectProperty = initialize(owlTopObjectPropertyIRI); 
	static final ObjectPropertyExpression owlBottomObjectProperty = initialize(owlBottomObjectPropertyIRI); 
	    
	private static ObjectPropertyExpression initialize(String uri) {
		final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
		Predicate prop = ofac.getObjectPropertyPredicate(uri);
		return new ObjectPropertyExpressionImpl(prop);  	
	}
	
	ObjectPropertyExpressionImpl(Predicate p) {
		this.predicate = p;
		this.isInverse = false;
		this.isTop = predicate.getName().equals(owlTopObjectPropertyIRI);
		this.isBottom = predicate.getName().equals(owlBottomObjectPropertyIRI);
		if (isTop || isBottom) 
			this.inverseProperty = this;   // [R1] in the grammar simplification rules
		else
			this.inverseProperty = new ObjectPropertyExpressionImpl(p, !isInverse, this);
		
		if (isInverse) {
			StringBuilder bf = new StringBuilder();
			bf.append(predicate.toString());
			bf.append("^-");
			this.string =  bf.toString();
		}
		else 
			this.string = predicate.toString();
		
		this.domain = new ObjectSomeValuesFromImpl(this);
	}
	
	/**
	 * special constructor for creating the inverse of an object property
	 * 
	 * @param p
	 * @param isInverse
	 * @param inverseProperty
	 */

	private ObjectPropertyExpressionImpl(Predicate p, boolean isInverse, ObjectPropertyExpressionImpl inverseProperty) {
		this.predicate = p;
		this.isInverse = isInverse;
		this.isTop = false; // cannot be the top property
		this.isBottom = false; // cannot be the bottom property
		this.inverseProperty = inverseProperty;
		StringBuilder bf = new StringBuilder();
		bf.append(predicate.toString());
		if (isInverse) 
			bf.append("^-");
		this.string =  bf.toString();

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
			return (isInverse == other.isInverse) && predicate.equals(other.predicate);
		}
		
		// the two types of properties share the same name space

		if (obj instanceof DataPropertyExpressionImpl) {
			DataPropertyExpressionImpl other = (DataPropertyExpressionImpl) obj;
			return (isInverse == false) && predicate.equals(other.getPredicate());
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
