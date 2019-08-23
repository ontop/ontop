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

import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.spec.ontology.DataPropertyAssertion;
import it.unibz.inf.ontop.spec.ontology.DataPropertyExpression;


/**
 * Represents DataPropertyAssertion from the OWL 2 QL Specification
 * 
 * DataPropertyAssertion := 'DataPropertyAssertion' '(' axiomAnnotations
 * 					DataPropertyExpression sourceIndividual targetValue ')'
 * DataPropertyExpression := DataProperty
 * 
 * Support for owl:topDataProperty and owl:bottomDataProperty
 * 
 * @author Roman Kontchakov
 *
 */

public class DataPropertyAssertionImpl implements DataPropertyAssertion {

	private final DataPropertyExpression prop;
	private final ObjectConstant o1;
	private final ValueConstant o2;


	DataPropertyAssertionImpl(DataPropertyExpression prop, ObjectConstant o1, ValueConstant o2) {
		this.prop = prop;
		this.o1 = o1;
		this.o2 = o2;
	}

	@Override
	public ObjectConstant getSubject() {
		return o1;
	}

	@Override
	public ValueConstant getValue() {
		return o2;
	}

	@Override
	public DataPropertyExpression getProperty() {
		return prop;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataPropertyAssertionImpl) {
			DataPropertyAssertionImpl other = (DataPropertyAssertionImpl)obj;
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

