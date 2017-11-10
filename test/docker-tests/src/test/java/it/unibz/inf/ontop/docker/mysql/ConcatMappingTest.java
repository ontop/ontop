package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Test;


/**
 * Class to test if CONCAT in mapping is working properly.
 * rdfs:label is a concat of literal and variables
 *

 */
public class ConcatMappingTest extends AbstractVirtualModeTest {

    static final String owlFile = "/mysql/northwind/mapping-northwind.owl";
    static final String obdaFile = "/mysql/northwind/mapping-northwind.obda";
    static final String propertiesFile = "/mysql/northwind/mapping-northwind.properties";

    public ConcatMappingTest() {
        super(owlFile, obdaFile, propertiesFile);
    }

    @Test
    public void testConcatQuery() throws Exception {
        String queryBind = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  \n" +
                "\n" +
                "SELECT  ?f ?y " +
                "WHERE {?f a <http://www.optique-project.eu/resource/northwind-h2-db/NORTHWIND/LOCATION> ; rdfs:label ?y .} \n";

        countResults(queryBind, 9);

    }
}

