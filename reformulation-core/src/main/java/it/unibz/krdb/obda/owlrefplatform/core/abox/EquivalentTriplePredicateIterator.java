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
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyExpression;
import it.unibz.krdb.obda.ontology.InconsistentOntologyException;
import it.unibz.krdb.obda.ontology.OClass;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyExpression;
import it.unibz.krdb.obda.ontology.OntologyFactory;
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
	
	
	private Assertion getNormal(Assertion assertion) {
		
		try {
			if (assertion instanceof ClassAssertion) {
				ClassAssertion ca = (ClassAssertion) assertion;
				OClass description = (OClass)reasoner.getClassDAG()
									.getCanonicalRepresentative(ca.getConcept());
				
				if (description != null) {
					ObjectConstant object = ca.getIndividual();
					return ofac.createClassAssertion(description, object);
				}			
			} 
			else if (assertion instanceof ObjectPropertyAssertion) {
				ObjectPropertyAssertion opa = (ObjectPropertyAssertion) assertion;
				ObjectPropertyExpression property = reasoner.getObjectPropertyDAG()
										.getCanonicalRepresentative(opa.getProperty());
				
				if (property != null) {
					ObjectConstant object1 = opa.getSubject();
					ObjectConstant object2 = opa.getObject();
					return ofac.createObjectPropertyAssertion(property, object1, object2);
				}
			} 
			else if (assertion instanceof DataPropertyAssertion) {
				DataPropertyAssertion opa = (DataPropertyAssertion) assertion;
				DataPropertyExpression property = reasoner.getDataPropertyDAG()
										.getCanonicalRepresentative(opa.getProperty());
				
				if (property != null) {
					ObjectConstant object1 = opa.getSubject();
					ValueConstant constant = opa.getValue();
					return ofac.createDataPropertyAssertion(property, object1, constant);
				}
			} 
		} 
		catch (InconsistentOntologyException e) {
			throw new RuntimeException("InconsistentOntologyException: " + assertion);
		}					
		return assertion;
	}
}
