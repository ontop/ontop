package it.unibz.inf.ontop.docker.postgres;


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Test;

/**
 * Tests the usage of a FQDN in the target of a mapping that will be converted in a sub-view
 *   (because of a SELECT DISTINCT).
 */
public class FqdnInTargetTest extends AbstractVirtualModeTest
{

    static final String owlfile = "/pgsql/imdb/ontologyIMDB.owl";
    static final String obdafile = "/pgsql/imdb/ontologyIMDB-fqdn.obda";
	static final String propertiesfile = "/pgsql/imdb/ontologyIMDB-fqdn.properties";

	public FqdnInTargetTest() {
		super(owlfile, obdafile, propertiesfile);
	}

	@Test
    public void testIMDBSeries() throws Exception {
		String query1 = "PREFIX : <http://www.seriology.org/seriology#> SELECT DISTINCT ?p WHERE { ?p a :Series . } LIMIT 10";
		countResults(query1, 10);
    }

}
