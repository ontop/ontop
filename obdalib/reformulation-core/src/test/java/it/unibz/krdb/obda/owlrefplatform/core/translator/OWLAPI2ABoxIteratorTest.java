package it.unibz.krdb.obda.owlrefplatform.core.translator;

import it.unibz.krdb.obda.owlapi2.OWLAPI2ABoxIterator;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Assertion;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;

import junit.framework.TestCase;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class OWLAPI2ABoxIteratorTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testAssertionIterator() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

		Iterator<OWLAxiom> owliterator = ontology.getAxioms().iterator();
		Iterator<Assertion> aboxit = new OWLAPI2ABoxIterator(owliterator);
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
		OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

		Iterator<Assertion> aboxit = new OWLAPI2ABoxIterator(ontology.getAxioms());
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 9);
	}
	
	public void testAssertionEmptyIterable() throws Exception {

		Iterator<Assertion> aboxit = new OWLAPI2ABoxIterator(new HashSet<OWLAxiom>());
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
		OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

		Iterator<Assertion> aboxit = new OWLAPI2ABoxIterator(ontology);
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 9);
	}
	
	public void testAssertionEmptyOntology() throws Exception {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.createOntology(URI.create("emptyontology"));

		Iterator<Assertion> aboxit = new OWLAPI2ABoxIterator(ontology);
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
		manager.loadOntologyFromPhysicalURI((new File(owlfile1)).toURI());
		manager.loadOntologyFromPhysicalURI((new File(owlfile2)).toURI());
		manager.loadOntologyFromPhysicalURI((new File(owlfile3)).toURI());

		Iterator<Assertion> aboxit = new OWLAPI2ABoxIterator(manager.getOntologies());
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 9);
	}
	
	public void testAssertionEmptyOntologySet() throws Exception {

		Iterator<Assertion> aboxit = new OWLAPI2ABoxIterator(new HashSet<OWLOntology>());
		int count = 0;
		while (aboxit.hasNext()) {
			count += 1;
			aboxit.next();
		}
		assertTrue("Count: " + count, count == 0);
	}


}
