package it.unibz.krdb.obda.owlapi3;

/*
 * #%L
 * ontop-obdalib-owlapi3
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

import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.ClassAssertion;
import it.unibz.krdb.obda.ontology.DataPropertyAssertion;
import it.unibz.krdb.obda.ontology.ObjectPropertyAssertion;

import java.util.Iterator;

import org.semanticweb.owlapi.model.OWLIndividualAxiom;

public class QuestOWLIndividualAxiomIterator implements Iterator<OWLIndividualAxiom> {

	private OWLAPI3IndividualTranslator translator = new OWLAPI3IndividualTranslator();
	
	private Iterator<Assertion> assertions = null;

	public QuestOWLIndividualAxiomIterator(Iterator<Assertion> assertions) {
		this.assertions = assertions;
	}
	
	@Override
	public boolean hasNext() {
		return assertions.hasNext();
	}

	@Override
	public OWLIndividualAxiom next() {
		Assertion assertion = assertions.next();
		OWLIndividualAxiom individual = translate(assertion);
		return individual;
	}

	private OWLIndividualAxiom translate(Assertion a) {
		if (a instanceof ClassAssertion) 
			return translator.translate((ClassAssertion) a);
		else if (a instanceof ObjectPropertyAssertion) 
			return translator.translate((ObjectPropertyAssertion) a);
		else
			return translator.translate((DataPropertyAssertion) a);
	}
	
	
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("This iterator is read-only");
	}
}
