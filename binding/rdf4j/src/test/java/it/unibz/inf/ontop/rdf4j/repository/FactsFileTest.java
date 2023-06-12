package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nullable;

/***
 * A test querying triples provided by an external "facts" file.
 */
public abstract class FactsFileTest extends AbstractRDF4JTest {

	@AfterClass
	public static void tearDown() throws Exception {
		AbstractRDF4JTest.release();
	}

	protected static void init(@Nullable String factsFile, @Nullable String factsBaseIRI) throws Exception {
		AbstractRDF4JTest.initOBDAWithFacts("/facts/facts-h2.sql",
				"/facts/mapping.obda",
				"/facts/ontology.ttl",
				null,
				factsFile,
				factsBaseIRI);
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

		runQueryAndCompare(query, ImmutableList.of(
				"Big Company",
				"Some Factory",
				"The Fact Company"));
	}
}
