package it.unibz.inf.ontop.owlapi.utils;

/*
 * #%L
 * ontop-obdalib-owlapi
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

import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL.TranslationException;
import org.semanticweb.owlapi.model.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/***
 * A read only iterator that will translateAndClassify OWLAPI data assertions into ABox
 * assertions in ontop's API. This is used in our Statement classes (e.g.,
 * {@code QuestOWLStatement} and SemanticIndexManager to iterate over the input
 * and then insert it into the semantic index database.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class OWLAPIABoxIterator implements Iterator<Assertion> {

	private final Iterator<OWLOntology> ontologiesIterator;

	private Iterator<OWLAxiom> owlaxiomIterator = null;
	private Assertion next = null;

	private final ClassifiedTBox tbox;
	private final OWLAPITranslatorOWL2QL owlapiTranslator;

	/**
	 * @param ontologies used only for data (ABox)
     * @param tbox provided the vocabulary for created ABox assertions
	 * @param owlapiTranslator
	 */

	public OWLAPIABoxIterator(Collection<OWLOntology> ontologies, ClassifiedTBox tbox,
							  OWLAPITranslatorOWL2QL owlapiTranslator) {
	    this.tbox = tbox;
		ontologiesIterator = ontologies.iterator();
		this.owlapiTranslator = owlapiTranslator;
		if (ontologiesIterator.hasNext()) 
			owlaxiomIterator = ontologiesIterator.next().getAxioms().iterator();
	}

	@Override
	public boolean hasNext() {
		while (true) {
			try {
				boolean hasnext = hasNextInCurrentIterator();
				if (hasnext) {
					return true;
				} else {
					try {
						switchToNextIterator();
					} catch (NoSuchElementException e) {
						return false;
					}
				}
			} catch (NoSuchElementException e) {
				try {
					switchToNextIterator();
				} catch (NoSuchElementException e2) {
					return false;
				}

			}
		}
	}

	@Override
	public Assertion next() {
		while (true) {
			try {
				return nextInCurrentIterator();
			} catch (NoSuchElementException e) {
				switchToNextIterator();
			}
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("This iterator is read-only");

	}

	/***
	 * Tries to advance to the next ontology in the iterator.
	 * 
	 * @throws NoSuchElementException
	 */
	private void switchToNextIterator() throws NoSuchElementException {
		OWLOntology nextOntology = ontologiesIterator.next();
		owlaxiomIterator = nextOntology.getAxioms().iterator();
	}

	/***
	 * Gives the next individual axiom in the current iterator. If none is found
	 * it will throw no such element exception.
	 * 
	 * @return
	 * @throws NoSuchElementException
	 */
	private Assertion nextInCurrentIterator() throws NoSuchElementException {

		if (owlaxiomIterator == null)
			throw new NoSuchElementException();

		if (next != null) {
			Assertion out = next;
			next = null;
			return out;
		}

		while (true) {
			OWLAxiom currentABoxAssertion = owlaxiomIterator.next();
	
			Assertion ax = translate(currentABoxAssertion);
			if (ax != null)
				return ax;
		}
	}
	
	private Assertion translate(OWLAxiom axiom) {

		try {
			if (axiom instanceof OWLClassAssertionAxiom)
                return owlapiTranslator.translate((OWLClassAssertionAxiom)axiom, tbox.classes());
			else if (axiom instanceof OWLObjectPropertyAssertionAxiom)
                return owlapiTranslator.translate((OWLObjectPropertyAssertionAxiom)axiom, tbox.objectProperties());
			else if (axiom instanceof OWLDataPropertyAssertionAxiom)
                return owlapiTranslator.translate((OWLDataPropertyAssertionAxiom)axiom, tbox.dataProperties());
		}
		catch (TranslationException | InconsistentOntologyException e) {
			return null;
		} 
        return null;
	}

	private boolean hasNextInCurrentIterator() {
		if (owlaxiomIterator == null)
			return false;
		
		while (true) {
			OWLAxiom currentABoxAssertion = owlaxiomIterator.next();

			Assertion ax = translate(currentABoxAssertion);
			if (ax != null) {
				next = ax;
				return true;
			}			
		}
	}
}
