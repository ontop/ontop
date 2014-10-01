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

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.ontology.Description;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/***
 * A read only iterator that will translate OWLAPI2 data assertions into ABox
 * assertions in ontop's API. This is used in our Statement classes (e.g.,
 * {@link QuestOWLStatement} and SemanticIndexManager to iterate over the input
 * and then insert it into the semantic index database.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class OWLAPI3ABoxIterator implements Iterator<Assertion> {

	private Iterator<OWLAxiom> owlaxiomiterator = null;
	private Iterator<OWLOntology> ontologies = null;
	private Assertion next = null;

	private final OWLAPI3Translator translator = new OWLAPI3Translator();
	private final Map<Predicate, Description> equivalenceMap;

	public OWLAPI3ABoxIterator(Collection<OWLOntology> ontologies, Map<Predicate, Description> equivalenceMap) {
		this.equivalenceMap = equivalenceMap;
		if (ontologies.size() > 0) {
			this.ontologies = ontologies.iterator();
			this.owlaxiomiterator = this.ontologies.next().getAxioms().iterator();
		}
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
		if (ontologies == null) {
			throw new NoSuchElementException();
		}

		OWLOntology nextOntology = ontologies.next();
		owlaxiomiterator = nextOntology.getAxioms().iterator();
	}

	/***
	 * Gives the next individual axiom in the current iterator. If none is found
	 * it will throw no such element exception.
	 * 
	 * @return
	 * @throws NoSuchElementException
	 */
	private Assertion nextInCurrentIterator() throws NoSuchElementException {

		if (owlaxiomiterator == null)
			throw new NoSuchElementException();

		if (next != null) {
			Assertion out = next;
			next = null;
			return out;
		}

		while (true) {
			OWLAxiom currentABoxAssertion = owlaxiomiterator.next();
			
			if (currentABoxAssertion instanceof OWLIndividualAxiom) {						
				Assertion ax = translator.translate((OWLIndividualAxiom) currentABoxAssertion, equivalenceMap);
				if (ax != null)
					return ax;
			}
		}
	}

	private boolean hasNextInCurrentIterator() {
		if (owlaxiomiterator == null)
			return false;
/*
		OWLAxiom currentABoxAssertion = null;

		try {
			currentABoxAssertion = owlaxiomiterator.next();
		} catch (NoSuchElementException e) {
			return false;
		}
*/
		
		while (true) {
			OWLAxiom currentABoxAssertion = owlaxiomiterator.next();
			
			if (currentABoxAssertion instanceof OWLIndividualAxiom) {						
				Assertion ax = translator.translate((OWLIndividualAxiom) currentABoxAssertion, equivalenceMap);
				if (ax != null) {
					next = ax;
					return true;
				}
			}
		}
	}

}
