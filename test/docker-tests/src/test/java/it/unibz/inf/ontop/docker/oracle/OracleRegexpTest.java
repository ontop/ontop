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
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import static org.junit.Assert.*;


/***
 * Tests that the system can handle the SPARQL "LIKE" keyword in the oracle setting
 * (i.e. that it is translated to REGEXP_LIKE and not LIKE in oracle sql)
 */
public class OracleRegexpTest extends AbstractVirtualModeTest {

	static final String owlfile = "/oracle/regex/oracle-regexp.owl";
	static final String obdafile = "/oracle/regex/oracle-regexp.obda";
	static final String propertyfile = "/oracle/regex/oracle-regexp.properties";

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


	private String runTest(String query, boolean hasResult) throws Exception {
		String retval;
		try (OWLStatement st = createStatement();
			 TupleOWLResultSet rs = st.executeSelectQuery(query)) {
			if (hasResult) {
				assertTrue(rs.hasNext());
				final OWLBindingSet bindingSet = rs.next();
				OWLIndividual ind1 = bindingSet.getOWLIndividual("country");
				retval = ind1.toString();
			} else {
				assertFalse(rs.hasNext());
				retval = "";
			}
			return retval;
		}
	}

	/**
	 * Tests the use of SPARQL like
	 * @throws Exception
	 */
	@Test
	public void testSparql2OracleRegex() throws Exception {
		try {

			String[] queries = {
					"'E[a-z]*t'", 
					"'^E[a-z]*t$'", 
					"'^E[a-z]*t$', 'm'", 
					"'Eg'", 
					"'^e[g|P|y]*T$', 'i'",
					"'^Egypt$', 'sm'"
					};
			for (String regex : queries){
				String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?country WHERE {?country a :Country; :name ?country_name . FILTER regex(?country_name, " + regex + ")}";
				String countryName = runTest(query, true);
				assertEquals(countryName, "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-Egypt>");
			}
			String[] wrongs = {
					"'eGYPT'", 
					"'^Eg$'",
					"'.Egypt$'"
					};
			for (String regex : wrongs){
				String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?country WHERE {?country a :Country; :name ?country_name . FILTER regex(?country_name, " + regex + ")}";
				String countryName = runTest(query, false);
				assertEquals(countryName, "");
			}
		} catch (Exception e) {
			throw e;
		}
	}

}
