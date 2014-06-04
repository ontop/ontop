package org.semanticweb.ontop.owlrefplatform.core.abox;

/*
 * #%L
 * ontop-reformulation-core
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

import java.util.Iterator;
import java.util.Map;

import org.semanticweb.ontop.model.ObjectConstant;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.URIConstant;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.ontology.ClassAssertion;
import org.semanticweb.ontop.ontology.DataPropertyAssertion;
import org.semanticweb.ontop.ontology.Description;
import org.semanticweb.ontop.ontology.OClass;
import org.semanticweb.ontop.ontology.ObjectPropertyAssertion;
import org.semanticweb.ontop.ontology.OntologyFactory;
import org.semanticweb.ontop.ontology.Property;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;

public class EquivalentTriplePredicateIterator implements Iterator<Assertion> {

	private Iterator<Assertion> originalIterator;
	private Map<Predicate, Description> equivalenceMap;
	
	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();
	
	public EquivalentTriplePredicateIterator(Iterator<Assertion> iterator, Map<Predicate, Description> equivalences) {
		originalIterator = iterator;
		equivalenceMap = equivalences;
	}
	
	@Override
	public boolean hasNext() {
		return originalIterator.hasNext();
	}

	@Override
	public Assertion next() {
		Assertion assertion = originalIterator.next();
		if (assertion instanceof ClassAssertion) {
			ClassAssertion ca = (ClassAssertion) assertion;
			Predicate concept = ca.getConcept();
			ObjectConstant object = ca.getObject();
			
			Description description = equivalenceMap.get(concept);
			if (description != null) {
				return ofac.createClassAssertion(((OClass) description).getPredicate(), object);
			}			
		} else if (assertion instanceof ObjectPropertyAssertion) {
			ObjectPropertyAssertion opa = (ObjectPropertyAssertion) assertion;
			Predicate role = opa.getRole();
			ObjectConstant object1 = opa.getFirstObject();
			ObjectConstant object2 = opa.getSecondObject();
			
			Description description = equivalenceMap.get(role);
			if (description != null) {
				Property property = (Property) description;
				if (property.isInverse()) {
					return ofac.createObjectPropertyAssertion(property.getPredicate(), object2, object1);
				} else {
					return ofac.createObjectPropertyAssertion(property.getPredicate(), object1, object2);
				}
			}
		} else if (assertion instanceof DataPropertyAssertion) {
			DataPropertyAssertion dpa = (DataPropertyAssertion) assertion;
			Predicate attribute = dpa.getAttribute();
			ObjectConstant object = dpa.getObject();
			ValueConstant constant = dpa.getValue();
			
			Description description = equivalenceMap.get(attribute);
			if (description != null) {
				return ofac.createDataPropertyAssertion(((Property) description).getPredicate(), object, constant);
			}
		}
		return assertion;
	}

	@Override
	public void remove() {
		originalIterator.remove();
	}
}
