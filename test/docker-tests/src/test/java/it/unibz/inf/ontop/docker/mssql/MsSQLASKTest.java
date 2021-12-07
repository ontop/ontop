package it.unibz.inf.ontop.docker.mssql;


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import static org.junit.Assert.assertTrue;

public class MsSQLASKTest extends AbstractVirtualModeTest {


    private static final String owlfile =
            "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange.owl";
    private static final String obdafile =
            "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange-mssql.obda";
    private static final String propertyfile =
            "/testcases-docker/virtual-mode/stockexchange/simplecq/stockexchange-mssql.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlfile, obdafile, propertyfile);
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
    public void testTrue() throws Exception {
        String query = "ASK { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.owl-ontologies.com/Ontology1207768242.owl#StockBroker> .}";
        boolean val =  runQueryAndReturnBooleanX(query);
        assertTrue(val);

    }
}