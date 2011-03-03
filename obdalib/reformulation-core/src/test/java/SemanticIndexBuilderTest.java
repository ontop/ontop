import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import junit.framework.TestCase;

import org.obda.owlrefplatform.core.abox.SemanticIndexBuilder;
import org.obda.owlrefplatform.core.abox.SemanticIndexRange;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public class SemanticIndexBuilderTest extends TestCase {

	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();;
	private OWLOntology ontology = null;
	private String owlloc = "src/test/resources/test/semanticIndex_ontologies/";

	private Map<OWLEntity, SemanticIndexRange> build_index(String ontoName)
			throws OWLOntologyCreationException {
		String owlfile = owlloc + ontoName + ".owl";
		ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile))
				.toURI());

		HashSet<OWLOntology> onto_set = new HashSet<OWLOntology>(1);
		onto_set.add(ontology);

		return SemanticIndexBuilder.build(onto_set);

	}

	private void compareIndexes(Map<OWLEntity, SemanticIndexRange> results,
			Map<String, SemanticIndexRange> expected) {

		assertEquals(expected.size(), results.size());
		for (OWLEntity i : results.keySet()) {
			String name = i.toString();
			SemanticIndexRange exp_range = expected.get(name);
			SemanticIndexRange res_range = results.get(i);
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

		Map<OWLEntity, SemanticIndexRange> results = build_index("test_1_0_0");
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

		Map<OWLEntity, SemanticIndexRange> results = build_index("test_1_0_1");
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

		Map<OWLEntity, SemanticIndexRange> results = build_index("test_1_1_0");
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

		Map<OWLEntity, SemanticIndexRange> results = build_index("test_1_2_0");
		compareIndexes(results, expected);
	}

}
