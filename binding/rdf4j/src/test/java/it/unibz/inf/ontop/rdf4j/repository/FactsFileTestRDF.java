package it.unibz.inf.ontop.rdf4j.repository;

import org.junit.BeforeClass;

/***
 * A test querying triples provided by an external "facts" file.
 */
public class FactsFileTestRDF extends FactsFileTest {

	@BeforeClass
	public static void setUp() throws Exception {
		init("/facts/facts.rdf", "http://ontop-vkg.org/facts#");
	}

}
