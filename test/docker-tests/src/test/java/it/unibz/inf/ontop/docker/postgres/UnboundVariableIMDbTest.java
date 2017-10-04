package it.unibz.inf.ontop.docker.postgres;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Test;


/**
 * Test class to solve the bug that generates unbound variables in the mapping.
 * Use the postgres IMDB database and a simple obda file with the problematic mapping.
 *
 * Solved modifying the method enforce equalities in DatalogNormalizer
 * to consider the case of nested equivalences in mapping
 */
public class UnboundVariableIMDbTest extends AbstractVirtualModeTest {

	static final String owlfile = "/pgsql/imdb/ontologyIMDB.owl";
	static final String obdafile = "/pgsql/imdb/ontologyIMDBSimplify.obda";
	static final String propertyfile = "/pgsql/imdb/movieontology.properties";

	public UnboundVariableIMDbTest() {
		super(owlfile, obdafile, propertyfile);
	}

	@Test
	public void testIMDBSeries() throws Exception {
		String query = "PREFIX : <http://www.seriology.org/seriology#> SELECT DISTINCT ?p WHERE { ?p a :Series . } LIMIT 10";
		countResults(query, 10);
	}
}
