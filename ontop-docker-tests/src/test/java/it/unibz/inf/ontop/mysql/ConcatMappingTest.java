package it.unibz.inf.ontop.mysql;

import it.unibz.inf.ontop.AbstractVirtualModeTest;


/**
 * Class to test if CONCAT in mapping is working properly.
 * rdfs:label is a concat of literal and variables
 *

 */
public class ConcatMappingTest extends AbstractVirtualModeTest {

    static final String owlFile = "src/test/resources/mysql/northwind/mapping-northwind.owl";
    static final String obdaFile = "src/test/resources/mysql/northwind/mapping-northwind.obda";
    static final String propertiesFile = "src/test/resources/mysql/northwind/mapping-northwind.properties";

    public ConcatMappingTest() {
        super(owlFile, obdaFile, propertiesFile);
    }

    public void testConcatQuery() throws Exception {
        String queryBind = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  \n" +
                "\n" +
                "SELECT  ?f ?y " +
                "WHERE {?f a <http://www.optique-project.eu/resource/northwind-h2-db/NORTHWIND/LOCATION> ; rdfs:label ?y .} \n";

        countResults(queryBind, 9);

    }
}

