package it.unibz.inf.ontop.docker.postgres;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;


/**
 * Created by elem on 21/09/15.
 */
public class MetaMappingExpanderTest extends AbstractVirtualModeTest {

    private final static String owlFile = "/pgsql/EPNet.owl";
    private final static String obdaFile = "/pgsql/EPNet.obda";
    private final static String propertyFile = "/pgsql/EPNet.properties";

    private static EngineConnection CONNECTION;

    @BeforeClass
    public static void before() {
        CONNECTION = createReasoner(owlFile, obdaFile, propertyFile);
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
    }

    @Test
    public void testQuery() throws Exception {
        countResults(1, "PREFIX : <http://www.semanticweb.org/ontologies/2015/1/EPNet-ONTOP_Ontology#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
                "select ?x\n" +
                "where {\n" +
                "?x rdf:type :AmphoraSection4-4 .\n" +
                "}\n" +
                "limit 5\n");
    }
}
