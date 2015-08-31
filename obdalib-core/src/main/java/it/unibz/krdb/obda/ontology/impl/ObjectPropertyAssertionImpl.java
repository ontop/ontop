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

import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;

/**
 * Represents ObjectPropertyAssertion from OWL 2 QL Specification
 * 
 * ObjectPropertyAssertion := 'ObjectPropertyAssertion' '(' axiomAnnotations
 * 									ObjectPropertyExpression sourceIndividual targetIndividual ')'
 * ObjectPropertyExpression := ObjectProperty | InverseObjectProperty
 * InverseObjectProperty := 'ObjectInverseOf' '(' ObjectProperty ')'
 * 
 *  Support for owl:topObjectProperty and owl:bottomObjectProperty
 *     - the inverses of the two coincide with themselves 
 * 
 * @author Roman Kontchakov
 *
 */

public class ObjectPropertyAssertionImpl implements ObjectPropertyAssertion {

	private static final long serialVersionUID = -8834975903851540150L;
	
	private final ObjectPropertyExpression prop;
	private final ObjectConstant o2;
	private final ObjectConstant o1;

	ObjectPropertyAssertionImpl(ObjectPropertyExpression prop, ObjectConstant o1, ObjectConstant o2) {
		this.prop = prop;
		this.o1 = o1;
		this.o2 = o2;
	}

	@Override
	public ObjectConstant getSubject() {
		return o1;
	}

	@Override
	public ObjectConstant getObject() {
		return o2;
	}

	@Override
	public ObjectPropertyExpression getProperty() {
		return prop;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ObjectPropertyAssertionImpl) {
			ObjectPropertyAssertionImpl other = (ObjectPropertyAssertionImpl)obj;
			return prop.equals(other.prop) && o1.equals(other.o1)  && o2.equals(other.o2);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return prop.hashCode() + o1.hashCode() + o2.hashCode();
	}
	
	@Override
	public String toString() {
		return prop + "(" + o1 + ", " + o2 + ")";
	}

}
