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
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.Property;
import it.unibz.krdb.obda.ontology.PropertyAssertion;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasoner;

import java.util.Iterator;

public class EquivalentTriplePredicateIterator implements Iterator<Assertion> {

	private final Iterator<Assertion> originalIterator;
	private final TBoxReasoner reasoner;

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();	
	
	public EquivalentTriplePredicateIterator(Iterator<Assertion> iterator, TBoxReasoner reasoner) {
		originalIterator = iterator;
		this.reasoner = reasoner;
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
			Predicate concept = ca.getConcept().getPredicate();
			OClass description = reasoner.getClassRepresentative(concept);
			
			if (description != null) {
				ObjectConstant object = ca.getIndividual();
				return ofac.createClassAssertion(description, object);
			}			
		} 
		else if (assertion instanceof PropertyAssertion) {
			PropertyAssertion opa = (PropertyAssertion) assertion;
			Predicate role = opa.getProperty().getPredicate();
			Property property = reasoner.getPropertyRepresentative(role);
			
			if (property != null) {
				ObjectConstant object1 = opa.getSubject();
				if (opa.getValue2() instanceof ValueConstant) {
					ValueConstant constant = (ValueConstant)opa.getValue2();
					return ofac.createPropertyAssertion(property, object1, constant);					
				}
				else {
					ObjectConstant object2 = (ObjectConstant)opa.getValue2();
					if (property.isInverse()) {
						Property notinv = ofac.createObjectProperty(property.getPredicate().getName(), false);
						return ofac.createPropertyAssertion(notinv, object2, object1);
					} else {
						return ofac.createPropertyAssertion(property, object1, object2);
					}
				}
			}
		} 
		return assertion;
	}
}
