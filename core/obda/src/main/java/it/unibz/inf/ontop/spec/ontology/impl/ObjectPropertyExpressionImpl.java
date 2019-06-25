package it.unibz.inf.ontop.spec.ontology.impl;

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

import it.unibz.inf.ontop.model.vocabulary.OWL;
import it.unibz.inf.ontop.spec.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.spec.ontology.ObjectSomeValuesFrom;
import org.apache.commons.rdf.api.IRI;

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

	private final boolean isInverse;
	private final IRI iri;
	private final String string;
	private final ObjectPropertyExpressionImpl inverseProperty;
	
	private final ObjectSomeValuesFromImpl domain;
	
	private final boolean isTop, isBottom;
	
	static final ObjectPropertyExpression owlTopObjectProperty = new ObjectPropertyExpressionImpl(OWL.TOP_OBJECT_PROPERTY);
	static final ObjectPropertyExpression owlBottomObjectProperty = new ObjectPropertyExpressionImpl(OWL.BOTTOM_OBJECT_PROPERTY);

	ObjectPropertyExpressionImpl(IRI iri) {
		this.iri = iri;
		this.isInverse = false;
		this.string = iri.getIRIString();
		this.isTop = iri.equals(OWL.TOP_OBJECT_PROPERTY);
		this.isBottom = iri.equals(OWL.BOTTOM_OBJECT_PROPERTY);
		if (isTop || isBottom) 
			this.inverseProperty = this;   // rule [R6] 
		else
			this.inverseProperty = new ObjectPropertyExpressionImpl(iri, this);
		
		this.domain = new ObjectSomeValuesFromImpl(this);
	}



	/**
	 * special constructor for creating the inverse of an object property
	 *  (this constructor is never applied to the top and bottom properties)
	 *
	 * @param iri
	 * @param inverseProperty
	 */

	private ObjectPropertyExpressionImpl(IRI iri, ObjectPropertyExpressionImpl inverseProperty) {
		this.iri = iri;
		this.isInverse = true; // always inverted
		this.isTop = false; // cannot be the top property
		this.isBottom = false; // cannot be the bottom property
		this.inverseProperty = inverseProperty;
		this.string = iri.getIRIString() + "^-"; // always inverted
		this.domain = new ObjectSomeValuesFromImpl(this);
	}

	@Override
	public boolean isInverse() {
		return isInverse;
	}

	@Override
	public IRI getIRI() {
		return iri;
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
		if (obj == this)
			return true;

		if (obj instanceof ObjectPropertyExpressionImpl) {
			ObjectPropertyExpressionImpl other = (ObjectPropertyExpressionImpl) obj;
			return (isInverse == other.isInverse) && iri.equals(other.iri);
		}
		
		// the two types of properties share the same name space
		if (obj instanceof DataPropertyExpressionImpl) {
			DataPropertyExpressionImpl other = (DataPropertyExpressionImpl) obj;
			return !isInverse && iri.equals(other.getIRI());
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
