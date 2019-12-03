package it.unibz.inf.ontop.docker.oracle;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import static org.junit.Assert.assertEquals;

/**
 *  Class to test that a URI with double prefix has not a prefix  wrongly removed
 *
 *
 */
public class OraclePrefixSourceTest extends AbstractVirtualModeTest {

    static final String owlFile = "/oracle/regex/stockBolzanoAddress.owl";
    static final String obdaFile = "/oracle/regex/stockexchangePrefix.obda";
    static final String propertiesFile = "/oracle/oracle.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertiesFile);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    @Test
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

