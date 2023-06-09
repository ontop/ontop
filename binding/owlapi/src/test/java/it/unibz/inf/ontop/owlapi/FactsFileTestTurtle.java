package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/***
 * A test querying triples provided by an external "facts" file.
 */
public class FactsFileTestTurtle extends AbstractOWLAPITest {

	@BeforeClass
	public static void setUp() throws Exception {
		initOBDA("/facts/facts-h2.sql",
				"/facts/mapping.obda",
				"/facts/ontology.ttl",
				null,
				"/facts/facts.ttl");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		release();
	}

	@Test
	public void testFactsIncluded() throws Exception {
		String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
				"PREFIX beam:   <https://data.beamery.com/ontologies/talent#>\n" +
				"PREFIX foaf:   <http://xmlns.com/foaf/0.1/>\n" +
				"PREFIX dct:    <http://purl.org/dc/terms/>\n" +
				"PREFIX schema: <https://schema.org/>\n" +
				"PREFIX : <http://www.semanticweb.org/ontop-facts#>\n" +
				"\n" +
				"SELECT DISTINCT * WHERE {\n" +
				"  ?c a :Company .\n" +
				"  ?c :name ?v.\n" +
				"} ORDER BY ?v\n";

		checkReturnedValues(query, "v", ImmutableList.of(
				"\"Big Company\"^^xsd:string",
				"\"Some Factory\"^^xsd:string",
				"\"The Fact Company\"^^xsd:string"));
	}
}
