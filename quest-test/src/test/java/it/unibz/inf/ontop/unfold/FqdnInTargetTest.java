package it.unibz.inf.ontop.unfold;


import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;

/**
 * Tests the usage of a FQDN in the target of a mapping that will be converted in a sub-view
 *   (because of a SELECT DISTINCT).
 */
public class FqdnInTargetTest extends AbstractVirtualModeTest
{

    static final String owlfile = "src/test/resources/ontologyIMDB.owl";
    static final String obdafile = "src/test/resources/ontologyIMDB-fqdn.obda";

	public FqdnInTargetTest() {
		super(owlfile, obdafile);
	}


    public void testIMDBSeries() throws Exception {
		String query1 = "PREFIX : <http://www.seriology.org/seriology#> SELECT DISTINCT ?p WHERE { ?p a :Series . } LIMIT 10";
		countResults(query1, 10);
    }

}
