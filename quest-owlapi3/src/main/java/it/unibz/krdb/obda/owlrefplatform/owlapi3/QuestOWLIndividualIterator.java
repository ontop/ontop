/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.owlapi3.OWLAPI3IndividualTranslator;

import java.util.Iterator;

import org.semanticweb.owlapi.model.OWLIndividualAxiom;

public class QuestOWLIndividualIterator  implements Iterator<OWLIndividualAxiom> {

	private OWLAPI3IndividualTranslator translator = new OWLAPI3IndividualTranslator();
	
	private Iterator<Assertion> assertions = null;

	public QuestOWLIndividualIterator(Iterator<Assertion> assertions) {
		this.assertions = assertions;
	}
	
	@Override
	public boolean hasNext() {
		return assertions.hasNext();
	}

	@Override
	public OWLIndividualAxiom next() {
		Assertion assertion = assertions.next();
		OWLIndividualAxiom individual = translator.translate(assertion);
		return individual;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("This iterator is read-only");
	}
}
