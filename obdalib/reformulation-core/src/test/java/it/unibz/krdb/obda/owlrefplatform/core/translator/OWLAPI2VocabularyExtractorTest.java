package it.unibz.krdb.obda.owlrefplatform.core.translator;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlapi2.OWLAPI2VocabularyExtractor;

import java.io.File;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class OWLAPI2VocabularyExtractorTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testOntology() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/translation/onto2.owl";

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

		OWLAPI2VocabularyExtractor ext = new OWLAPI2VocabularyExtractor();
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
		manager.loadOntologyFromPhysicalURI((new File(owlfile1)).toURI());
		manager.loadOntologyFromPhysicalURI((new File(owlfile2)).toURI());
		manager.loadOntologyFromPhysicalURI((new File(owlfile3)).toURI());

		OWLAPI2VocabularyExtractor ext = new OWLAPI2VocabularyExtractor();
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
