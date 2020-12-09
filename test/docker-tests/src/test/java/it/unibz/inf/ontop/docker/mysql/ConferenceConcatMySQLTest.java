package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test
 * CONCAT with table.columnName and string values that need to be change to literal
 * use mysql.
 *
 */

public class ConferenceConcatMySQLTest extends AbstractVirtualModeTest {

    private static final String owlFile = "/mysql/conference/ontology3.owl";
    private static final String obdaFile = "/mysql/conference/secondmapping-test.obda";
	private static final String propertyFile = "/mysql/conference/secondmapping-test.properties";

	private static OntopOWLReasoner REASONER;
	private static OntopOWLConnection CONNECTION;

	@BeforeClass
	public static void before() throws OWLOntologyCreationException {
		REASONER = createReasoner(owlFile, obdaFile, propertyFile);
		CONNECTION = REASONER.getConnection();
	}

	@Override
	protected OntopOWLStatement createStatement() throws OWLException {
		return CONNECTION.createStatement();
	}

	@AfterClass
	public static void after() throws OWLException {
		CONNECTION.close();
		REASONER.dispose();
	}

	private void runTests(String query1) throws Exception {

		OWLStatement st = createStatement();


		try {
			executeQueryAssertResults(query1, st);
			
		} catch (Exception e) {
            st.close();
            e.printStackTrace();
            assertTrue(false);


		} finally {
			st.close();
		}
	}

	private void executeQueryAssertResults(String query, OWLStatement st) throws Exception {
		TupleOWLResultSet rs = st.executeSelectQuery(query);

		OWLObject answer, answer2;
		rs.hasNext();
        final OWLBindingSet bindingSet = rs.next();
        answer= bindingSet.getOWLObject("x");
		System.out.print("x =" + answer);
		System.out.print(" ");
		answer2= bindingSet.getOWLObject("y");

		System.out.print("y =" + answer2);
		System.out.print(" ");

		rs.close();
		assertEquals("<http://myproject.org/odbs#tracepaper1>", answer.toString());
		assertEquals("<http://myproject.org/odbs#eventpaper1>", answer2.toString());
	}

	@Test
	public void testConcat() throws Exception {

        String query1 = "PREFIX : <http://myproject.org/odbs#> SELECT ?x ?y\n" +
                "WHERE {\n" +
                "   ?x :TcontainsE ?y\n" +
				"}";

		runTests(query1);
	}


}
