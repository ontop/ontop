package it.unibz.inf.ontop.docker.oracle;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;

import static org.junit.Assert.*;


/***
 * Tests that the system can handle the SPARQL "LIKE" keyword in the oracle setting
 * (i.e. that it is translated to REGEXP_LIKE and not LIKE in oracle sql)
 */
public class OracleRegexpSpaceTest extends AbstractVirtualModeTest {

	static final String owlFile = "/oracle/regex/oracle-regexp.owl";
	static final String obdaFile = "/oracle/regex/oracle-regexp.obda";
	static final String propertyFile = "/oracle/regex/oracle-regexp.properties";

	private static EngineConnection CONNECTION;

	@BeforeClass
	public static void before()  {
		CONNECTION = createReasoner(owlFile, obdaFile, propertyFile);
	}

	@Override
	protected OntopOWLStatement createStatement() throws OWLException {
		return CONNECTION.createStatement();
	}

	@AfterClass
	public static void after() throws Exception {
		CONNECTION.close();
	}


	private String runTest(OWLStatement st, String query, boolean hasResult) throws Exception {
		String retval;
		TupleOWLResultSet rs = st.executeSelectQuery(query);
		if(hasResult){
			assertTrue(rs.hasNext());
            final OWLBindingSet bindingSet = rs.next();
            OWLIndividual ind1 = bindingSet.getOWLIndividual("country")	 ;
			retval = ind1.toString();
		} else {
			assertFalse(rs.hasNext());
			retval = "";
		}

		return retval;
	}

	/**
	 * Tests the use of mapings with regex in subqueries and where with SQL subquery
	 * @throws Exception
	 */
	@Test
	public void testSparql2OracleRegexWhere() throws Exception {
		try (OWLStatement st = createStatement()) {
			String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?country WHERE {?country a :CountryWithSpace . } ORDER BY ?country LIMIT 1";
			String countryName = runTest(st, query, true);
			System.out.println(countryName);
			assertEquals(countryName, "<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-United%20Kingdom>");
		}
	}
	
	
	/**
	 * Tests the use of mapings with regex in subqueries without where with SQL subquery
	 * @throws Exception
	 */
	@Test
	public void testSparql2OracleRegexNoWhere() throws Exception {
		try (OWLStatement st = createStatement()) {
			String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT ?country ?pos WHERE {" +
					"?country a :CountriesWithSpaceNoWhere . " +
					"?country :position ?pos . " +
					"FILTER (?pos >0)" +
					"}\n" +
					"ORDER BY ?country";
			String countryName = runTest(st, query, true);
			System.out.println(countryName);
			assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-United%20Kingdom>", countryName);


		}
	}

	/**
	 * Tests the use of mapings with regex in subqueries without where without subquery
	 * @throws Exception
	 */
	@Test
	public void testSparql2OracleRegexNoWhereNoSubquery() throws Exception {
		try (OWLStatement st = createStatement()) {
			String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> " +
					"SELECT ?country ?pos WHERE {" +
					"  ?country a :CountriesWithSpaceNoWhereNoSubquery . " +
					"  ?country :position ?pos . " +
					"  FILTER (?pos >0)" +
					"}\n" +
					"ORDER BY ?country";
			String countryName = runTest(st, query, true);
			System.out.println(countryName);
			assertEquals("<http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#Country-United%20Kingdom>", countryName);

		}
	}

	

}
