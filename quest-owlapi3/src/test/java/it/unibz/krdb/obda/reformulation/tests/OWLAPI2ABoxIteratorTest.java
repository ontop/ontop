package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.ontology.Assertion;
import it.unibz.krdb.obda.owlapi3.OWLAPI3ABoxIterator;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OWLAPI2ABoxIteratorTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testAssertionIterator() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		Iterator<OWLAxiom> owliterator = ontology.getAxioms().iterator();
		Iterator<Assertion> aboxit = new OWLAPI3ABoxIterator(owliterator);
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

		Iterator<Assertion> aboxit = new OWLAPI3ABoxIterator(ontology.getAxioms());
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 9);
	}
	
	public void testAssertionEmptyIterable() throws Exception {

		Iterator<Assertion> aboxit = new OWLAPI3ABoxIterator(new HashSet<OWLAxiom>());
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

		Iterator<Assertion> aboxit = new OWLAPI3ABoxIterator(ontology);
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

		Iterator<Assertion> aboxit = new OWLAPI3ABoxIterator(ontology);
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

		Iterator<Assertion> aboxit = new OWLAPI3ABoxIterator(manager.getOntologies());
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 9);
	}
	
	public void testAssertionEmptyOntologySet() throws Exception {

		Iterator<Assertion> aboxit = new OWLAPI3ABoxIterator(new HashSet<OWLOntology>());
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 0);
	}


}
