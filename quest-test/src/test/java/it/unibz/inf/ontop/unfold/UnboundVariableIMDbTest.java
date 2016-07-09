package it.unibz.inf.ontop.unfold;

import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;

/**
 * Test class to solve the bug that generates unbound variables in the mapping.
 * Use the postgres IMDB database and a simple obda file with the problematic mapping.
 *
 * Solved modifying the method enforce equalities in DatalogNormalizer
 * to consider the case of nested equivalences in mapping
 */
public class UnboundVariableIMDbTest extends AbstractVirtualModeTest {

	static final String owlfile = "src/test/resources/ontologyIMDB.owl";
	static final String obdafile = "src/test/resources/ontologyIMDBSimplify.obda";

	protected UnboundVariableIMDbTest() {
		super(owlfile, obdafile);
	}

	public void testIMDBSeries() throws Exception {
		String query = "PREFIX : <http://www.seriology.org/seriology#> SELECT DISTINCT ?p WHERE { ?p a :Series . } LIMIT 10";
		countResults(query, 10);
	}
}
