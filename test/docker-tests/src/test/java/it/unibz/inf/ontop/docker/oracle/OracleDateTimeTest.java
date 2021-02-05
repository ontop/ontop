package it.unibz.inf.ontop.docker.oracle;


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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class OracleDateTimeTest extends AbstractVirtualModeTest {

	private static final String owlfile = "/oracle/datetime/dateTimeExampleBooks.owl";
	private static final String obdafile = "/oracle/datetime/dateTimeExampleBooks.obda";
	private static final String propertyfile = "/oracle/datetime/dateTimeExampleBooks.properties";

	private static final Logger log = LoggerFactory.getLogger(OracleDateTimeTest.class);

	private static OntopOWLReasoner REASONER;
	private static OntopOWLConnection CONNECTION;

	@BeforeClass
	public static void before() throws OWLOntologyCreationException {
		REASONER = createReasoner(owlfile, obdafile, propertyfile);
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


	private String runTest(OWLStatement st, String query, boolean hasResult) throws Exception {
		String retval;
		TupleOWLResultSet rs = st.executeSelectQuery(query);
		if(hasResult){
			assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLObject ind1 = bindingSet.getOWLObject("y")	 ;
			retval = ind1.toString();
		} else {
			assertFalse(rs.hasNext());
			retval = "";
		}

		return retval;
	}

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void testDateTime() throws Exception {
		try (OWLStatement st = createStatement()) {

			String query = "PREFIX :	<http://meraka/moss/exampleBooks.owl#> \n " +
					" SELECT ?x ?y WHERE " +
					"{?x :dateOfPublication ?y .}\n" +
					"ORDER BY DESC(?y)";
			String date = runTest(st, query, true);
			log.debug(date);

			assertEquals("\"2010-05-01T10:02:30\"^^xsd:dateTime", date);

		}
	}

}
