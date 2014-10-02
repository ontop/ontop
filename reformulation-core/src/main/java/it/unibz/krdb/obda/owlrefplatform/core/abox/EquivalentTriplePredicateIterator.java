package it.unibz.krdb.obda.owlrefplatform.core.abox;

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

import it.unibz.krdb.obda.model.ObjectConstant;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.EquivalenceMap;

import java.util.Iterator;

public class EquivalentTriplePredicateIterator implements Iterator<Assertion> {

	private final Iterator<Assertion> originalIterator;
	private final EquivalenceMap equivalenceMap;

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();	
	
	public EquivalentTriplePredicateIterator(Iterator<Assertion> iterator, EquivalenceMap equivalences) {
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
		return getNormal(assertion);
	}

	@Override
	public void remove() {
		originalIterator.remove();
	}
	
	// used in EquivalentTriplePredicateIterator
	
	private Assertion getNormal(Assertion assertion) {
		if (assertion instanceof ClassAssertion) {
			ClassAssertion ca = (ClassAssertion) assertion;
			Predicate concept = ca.getConcept();
			OClass description = equivalenceMap.getClassRepresentative(concept);
			
			if (description != null) {
				ObjectConstant object = ca.getObject();
				return ofac.createClassAssertion(description.getPredicate(), object);
			}			
		} 
		else if (assertion instanceof ObjectPropertyAssertion) {
			ObjectPropertyAssertion opa = (ObjectPropertyAssertion) assertion;
			Predicate role = opa.getRole();
			Property property = equivalenceMap.getPropertyRepresentative(role);
			
			if (property != null) {
				ObjectConstant object1 = opa.getFirstObject();
				ObjectConstant object2 = opa.getSecondObject();
				if (property.isInverse()) {
					return ofac.createObjectPropertyAssertion(property.getPredicate(), object2, object1);
				} else {
					return ofac.createObjectPropertyAssertion(property.getPredicate(), object1, object2);
				}
			}
		} 
		else if (assertion instanceof DataPropertyAssertion) {
			DataPropertyAssertion dpa = (DataPropertyAssertion) assertion;
			Predicate attribute = dpa.getAttribute();
			Property property = equivalenceMap.getPropertyRepresentative(attribute);
			
			if (property != null) {
				ObjectConstant object = dpa.getObject();
				ValueConstant constant = dpa.getValue();
				return ofac.createDataPropertyAssertion(property.getPredicate(), object, constant);
			}
		}
		return assertion;
	}
}
