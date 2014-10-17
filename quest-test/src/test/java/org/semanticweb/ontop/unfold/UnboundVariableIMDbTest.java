package org.semanticweb.ontop.unfold;


import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Test class to solve the bug that generates unbound variables in the mapping.
 * Use the postgres IMDB database and a simple obda file with the problematic mapping.
 *
 * Solved modifying the method enforce equalities in DatalogNormalizer
 * to consider the case of nested equivalences in mapping
 */
public class UnboundVariableIMDbTest {

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OWLOntology ontology;

	final String owlFileName = "src/test/resources/ontologyIMDB.owl";
	final String obdaFileName = "src/test/resources/ontologyIMDBSimplify.obda";


	@Before
	public void setUp() throws Exception {
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlFileName)));
		
	}

	private void runTests(Properties p) throws Exception {
		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory(new File(obdaFileName), p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();

		String query1 = "PREFIX : <http://www.seriology.org/seriology#> SELECT DISTINCT ?p WHERE { ?p a :Series . } LIMIT 10";

	
		try {
			int results = executeQuerySPARQL(query1, st);
			assertEquals(10, results);
			
		} catch (Exception e) {

            assertTrue(false);
			log.error(e.getMessage());

		} finally {

		    st.close();
			conn.close();
			reasoner.dispose();
		}
	}
	
	public int executeQuerySPARQL(String query, QuestOWLStatement st) throws Exception {
		QuestOWLResultSet rs = st.executeTuple(query);
		int count = 0;
		while (rs.nextRow()) {

            count++;

			log.debug("result " + count + " "+ rs.getOWLObject("p"));

		}
		rs.close();

        return count;
	}
	


	@Test
	public void testIMDBSeries() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		runTests(p);
	}

}
