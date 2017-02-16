package it.unibz.inf.ontop.parser;

import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;

/**
 *  Class to test that a URI with double prefix has not a prefix  wrongly removed
 *
 *
 */
public class OraclePrefixSourceTest extends AbstractVirtualModeTest {

    static final String owlFile = "src/test/resources/regex/stockBolzanoAddress.owl";
    static final String obdaFile = "src/test/resources/stockexchangePrefix.obda";
    static final String propertiesFile = "src/test/resources/stockexchangePrefix.properties";

    public OraclePrefixSourceTest() {
        super(owlFile, obdaFile, propertiesFile);
    }


    public void testPrefixInsideURI() throws Exception {

        String queryBind = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#>" +
                "\n" +
                "SELECT  ?x " +
                "WHERE {?y :hasAddress ?x .  \n" +
                "}";

        assertEquals(runQueryAndReturnStringOfIndividualX(queryBind),
                "<http://example.com/resource/?repository=repo&uri=http://www.owl-ontologies.com/Ontology1207768242.owl/Address-991>");
    }
}

