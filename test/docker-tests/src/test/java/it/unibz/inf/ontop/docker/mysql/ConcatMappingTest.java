package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


/**
 * Class to test if CONCAT in mapping is working properly.
 * rdfs:label is a concat of literal and variables
 *

 */
public class ConcatMappingTest extends AbstractVirtualModeTest {

    private static final String owlFile = "/mysql/northwind/mapping-northwind.owl";
    private static final String obdaFile = "/mysql/northwind/mapping-northwind.obda";
    private static final String propertiesFile = "/mysql/northwind/mapping-northwind.properties";

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
    public void testConcatQuery() throws Exception {
        String queryBind = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  \n" +
                "\n" +
                "SELECT  ?f ?y " +
                "WHERE {?f a <http://www.optique-project.eu/resource/northwind-h2-db/NORTHWIND/LOCATION> ; rdfs:label ?y .} \n";

        countResults(9, queryBind);

    }
}

