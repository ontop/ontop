package it.unibz.inf.ontop.owlapi.utils;

/*
 * #%L
 * ontop-quest-owlapi
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

import com.google.common.collect.Lists;
import it.unibz.inf.ontop.spec.ontology.Assertion;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyFactoryImpl;
import it.unibz.inf.ontop.owlapi.utils.OWLAPIABoxIterator;
import it.unibz.inf.ontop.spec.ontology.impl.TBoxReasonerImpl;
import junit.framework.TestCase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

public class OWLAPIABoxIteratorTest extends TestCase {

	private TBoxReasoner reasoner;
	
	protected void setUp() throws Exception {
		OntologyVocabulary voc = OntologyFactoryImpl.getInstance().createVocabulary();
		voc.objectProperties().create("http://it.unibz.inf/obda/ontologies/test/translation/onto2.owl#P");
		voc.objectProperties().create("http://it.unibz.inf/obda/ontologies/test/translation/onto2.owl#R");
	    voc.dataProperties().create("http://it.unibz.inf/obda/ontologies/test/translation/onto2.owl#age");
	    voc.classes().create("http://it.unibz.inf/obda/ontologies/test/translation/onto2.owl#Man");
	    voc.classes().create("http://it.unibz.inf/obda/ontologies/test/translation/onto2.owl#Person");
	    voc.classes().create("http://it.unibz.inf/obda/ontologies/test/translation/onto2.owl#Woman");
	    Ontology onto = OntologyFactoryImpl.getInstance().createOntology(voc);
	    reasoner = TBoxReasonerImpl.create(onto);
		super.setUp();
	}

	public void testAssertionIterator() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		//Iterator<OWLAxiom> owliterator = ontology.getAxioms().iterator();
		Iterator<Assertion> aboxit = new OWLAPIABoxIterator(Lists.newArrayList(ontology), reasoner);
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 9);
	}

	public void testAssertionIterable() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		Iterator<Assertion> aboxit = new OWLAPIABoxIterator(Lists.newArrayList(ontology), reasoner);
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 9);
	}
	
	public void testAssertionEmptyIterable() throws Exception {

		Iterator<Assertion> aboxit = new OWLAPIABoxIterator(Lists.<OWLOntology>newArrayList(), reasoner);
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 0);
	}




	
	public void testAssertionOntology() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		Iterator<Assertion> aboxit = new OWLAPIABoxIterator(Lists.newArrayList(ontology), reasoner);
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 9);
	}
	
	public void testAssertionEmptyOntology() throws Exception {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.createOntology();

		Iterator<Assertion> aboxit = new OWLAPIABoxIterator(Lists.newArrayList(ontology), reasoner);
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 0);
	}
	
	public void testAssertionOntologies() throws Exception {
		String owlfile1 = "src/test/resources/test/ontologies/translation/onto1.owl";
		String owlfile2 = "src/test/resources/test/ontologies/translation/onto2.owl";
		String owlfile3 = "src/test/resources/test/ontologies/translation/onto3.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		manager.loadOntologyFromOntologyDocument((new File(owlfile1)));
		manager.loadOntologyFromOntologyDocument((new File(owlfile2)));
		manager.loadOntologyFromOntologyDocument((new File(owlfile3)));

		Iterator<Assertion> aboxit = new OWLAPIABoxIterator(manager.getOntologies(), reasoner);
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 9);
	}
	
	public void testAssertionEmptyOntologySet() throws Exception {

		Iterator<Assertion> aboxit = new OWLAPIABoxIterator(new HashSet<OWLOntology>(), reasoner);
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 0);
	}
}
