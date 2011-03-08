import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.obda.owlrefplatform.core.abox.DAG;
import org.obda.owlrefplatform.core.abox.SemanticIndexRange;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public class DAGTest extends TestCase {

	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();;
	private OWLOntology ontology = null;
	private String owlloc = "src/test/resources/test/semanticIndex_ontologies/";

	private DAG build_index(String ontoName)
			throws OWLOntologyCreationException {
		String owlfile = owlloc + ontoName + ".owl";
		ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile))
				.toURI());

		Set<OWLOntology> onto_set = new HashSet<OWLOntology>(1);
		onto_set.add(ontology);

		return new DAG(onto_set);

	}

	private void compareIndexes(DAG results,
			Map<String, SemanticIndexRange> expected) {

		// XXX: currently not checking if computed results don't containt more then expected
		for (String i : expected.keySet()) {
			SemanticIndexRange exp_range = expected.get(i);
			SemanticIndexRange res_range = results.getIndexRange(i);
			assertEquals(exp_range, res_range);
		}
	}

	public void test_1_0_0() throws OWLOntologyCreationException {
		HashMap<String, SemanticIndexRange> expected = new HashMap<String, SemanticIndexRange>() {
			{
				put("A", new SemanticIndexRange(1, 4));
				put("B", new SemanticIndexRange(2, 4));
				put("C", new SemanticIndexRange(3, 4));
				put("D", new SemanticIndexRange(4, 4));
			}
		};

		DAG results = build_index("test_1_0_0");
		compareIndexes(results, expected);
	}

	public void test_1_0_1() throws OWLOntologyCreationException {
		HashMap<String, SemanticIndexRange> expected = new HashMap<String, SemanticIndexRange>() {
			{
				put("A", new SemanticIndexRange(1, 13));
				put("B", new SemanticIndexRange(6, 9));
				put("C", new SemanticIndexRange(10, 13));
				put("D", new SemanticIndexRange(2, 5));
				put("E", new SemanticIndexRange(8, 8));
				put("F", new SemanticIndexRange(9, 9));
				put("G", new SemanticIndexRange(7, 7));
				put("H", new SemanticIndexRange(13, 13));
				put("I", new SemanticIndexRange(11, 11));
				put("J", new SemanticIndexRange(12, 12));
				put("K", new SemanticIndexRange(5, 5));
				put("L", new SemanticIndexRange(4, 4));
				put("M", new SemanticIndexRange(3, 3));
			}
		};

		DAG results = build_index("test_1_0_1");
		compareIndexes(results, expected);
	}

	public void test_1_1_0() throws OWLOntologyCreationException {
		HashMap<String, SemanticIndexRange> expected = new HashMap<String, SemanticIndexRange>() {
			{
				put("A", new SemanticIndexRange(1, 3));
				put("B", new SemanticIndexRange(3, 3));
				put("C", new SemanticIndexRange(2, 2));
				put("D", new SemanticIndexRange(2, 2).addInterval(4, 4));
			}
		};

		DAG results = build_index("test_1_1_0");
		System.out.println(results);
		compareIndexes(results, expected);
	}

	public void test_1_2_0() throws OWLOntologyCreationException {
		HashMap<String, SemanticIndexRange> expected = new HashMap<String, SemanticIndexRange>() {
			{
				put("A", new SemanticIndexRange(1, 3));
				put("B", new SemanticIndexRange(2, 3));
				put("C", new SemanticIndexRange(3, 3));
			}
		};

		DAG results = build_index("test_1_2_0");
		compareIndexes(results, expected);
	}

	public void test_1_3_0() throws OWLOntologyCreationException {
		HashMap<String, SemanticIndexRange> expected = new HashMap<String, SemanticIndexRange>() {
			{
				put("A", new SemanticIndexRange(1, 3));
				put("B", new SemanticIndexRange(2, 3));
				put("C", new SemanticIndexRange(3, 3));
			}
		};

		DAG results = build_index("test_1_3_0");
		compareIndexes(results, expected);
	}

	public void test_1_4_0() throws OWLOntologyCreationException {
		HashMap<String, SemanticIndexRange> expected = new HashMap<String, SemanticIndexRange>() {
			{
				put("A", new SemanticIndexRange(1, 3));
				put("B", new SemanticIndexRange(2, 3));
				put("C", new SemanticIndexRange(3, 3));
			}
		};

		DAG results = build_index("test_1_4_0");
		compareIndexes(results, expected);
	}

}
