package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.BeforeClass;
import org.junit.Test;

/***
 * A test querying triples provided by an external "facts" file.
 */
public class FactsFileTestNQuads extends FactsFileTest {

	@BeforeClass
	public static void setUp() throws Exception {
		init("/facts/facts.nq", null);
	}

	@Test
	public void testSubgraph() {
		String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
				"PREFIX beam:   <https://data.beamery.com/ontologies/talent#>\n" +
				"PREFIX foaf:   <http://xmlns.com/foaf/0.1/>\n" +
				"PREFIX dct:    <http://purl.org/dc/terms/>\n" +
				"PREFIX schema: <https://schema.org/>\n" +
				"PREFIX : <http://www.semanticweb.org/ontop-facts#>\n" +
				"\n" +
				"SELECT DISTINCT * WHERE {\n" +
				"  GRAPH :extra { ?s ?p ?v . } \n" +
				"} ORDER BY ?v\n";

		runQueryAndCompare(query, ImmutableList.of(
				"2022"));
	}
}
