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

public class DatetimestampR2rmlTest extends AbstractVirtualModeTest {
    private static final String owlFile = "/mysql/northwind/northwind-dmo.owl";
    private static final String r2rmlFile = "/mysql/northwind/mapping-northwind-dmo.ttl";
    private static final String propertyFile = "/mysql/northwind/mapping-northwind-dmo.properties";
    private static final String queriesFile = "/mysql/northwind/northwind.q";


    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createR2RMLReasoner(owlFile, r2rmlFile, propertyFile);
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
    public void testR2rml() throws Exception {
        runQueries(queriesFile);
    }
}
