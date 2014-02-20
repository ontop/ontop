package it.unibz.krdb.obda.reformulation.tests;

/*
 * #%L
 * ontop-quest-owlapi3
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
import it.unibz.krdb.obda.owlapi3.OWLAPI3VocabularyExtractor;

import java.io.File;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OWLAPI2VocabularyExtractorTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testOntology() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		OWLAPI3VocabularyExtractor ext = new OWLAPI3VocabularyExtractor();
		Set<Predicate> preds = ext.getVocabulary(ontology);
		int countClass = 0;
		int countProp = 0;
		for (Predicate p : preds) {
			if (p.getArity() == 1) {
				countClass += 1;
			} else if (p.getArity() == 2) {
				countProp += 1;
			} else {
				fail();
			}
		}
		assertTrue("Count Class: " + countClass, countClass == 3);
		assertTrue("Prop Class: " + countProp, countProp == 3);
	}

	public void testOntologies() throws Exception {
		String owlfile1 = "src/test/resources/test/ontologies/translation/onto1.owl";
		String owlfile2 = "src/test/resources/test/ontologies/translation/onto2.owl";
		String owlfile3 = "src/test/resources/test/ontologies/translation/onto3.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		manager.loadOntologyFromOntologyDocument((new File(owlfile1)));
		manager.loadOntologyFromOntologyDocument((new File(owlfile2)));
		manager.loadOntologyFromOntologyDocument((new File(owlfile3)));

		OWLAPI3VocabularyExtractor ext = new OWLAPI3VocabularyExtractor();
		Set<Predicate> preds = ext.getVocabulary(manager.getOntologies());
		int countClass = 0;
		int countProp = 0;
		for (Predicate p : preds) {
			if (p.getArity() == 1) {
				countClass += 1;
			} else if (p.getArity() == 2) {
				countProp += 1;
			} else {
				fail();
			}
		}
		assertTrue("Count Class: " + countClass, countClass == 5);
		assertTrue("Prop Class: " + countProp, countProp == 5);
	}

}
