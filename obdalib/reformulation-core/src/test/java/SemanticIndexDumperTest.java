import java.io.File;
import java.sql.Connection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.h2.jdbcx.JdbcDataSource;
import org.obda.owlrefplatform.core.abox.DAG;
import org.obda.owlrefplatform.core.abox.SemanticIndexDumper;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

import ch.qos.logback.core.db.dialect.H2Dialect;

public class SemanticIndexDumperTest extends TestCase {
	

	private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();;
	private OWLOntology ontology = null;
	private String owlloc = "src/test/resources/test/semanticIndex_ontologies/";
	private Connection conn;
	
	
	private void materialize_ontology(String ontoName) throws OWLOntologyCreationException {
		String owlfile = owlloc + ontoName + ".owl";
		OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile))
				.toURI());

		Set<OWLOntology> onto_set = new HashSet<OWLOntology>(1);
		onto_set.add(ontology);
		
		SemanticIndexDumper.materialize(onto_set, conn);
	}
	
	/**
	 * Get results dumped data from H2 database and compare with expected
	 * @param expected
	 */
	private void compareInserts(List<String> expected) {

	}

	protected void setUp() throws Exception {
		super.setUp();
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:db1");
		conn = ds.getConnection();
	}

	public void test_1_0_0() throws OWLOntologyCreationException {
		List<String> expected = new LinkedList<String>();
		
		materialize_ontology("test_1_0_0");
		compareInserts(expected);
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		conn.close();
	}

}
