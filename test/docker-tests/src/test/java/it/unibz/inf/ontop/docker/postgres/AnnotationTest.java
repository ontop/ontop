package it.unibz.inf.ontop.docker.postgres;


import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to test if annotation property can be treated as data property and object property
 *
 *
 */
@Ignore("Too slow (20 min)!")
public class AnnotationTest extends AbstractVirtualModeTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final static String owlFile = "/pgsql/annotation/doid.owl";
    private final static String obdaFile = "/pgsql/annotation/doid.obda";
    private final static String propertyFile = "/pgsql/annotation/doid.properties";

    private static OntopOWLEngine REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() {
        REASONER = createReasoner(owlFile, obdaFile, propertyFile);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
        REASONER.close();
    }

    @Test
    public void testAnnotationInOntology() throws Exception {
        String query =
                "PREFIX xsd:\t<http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT *\n" +
                "{ ?x rdfs:comment \"NT MGI.\"^^xsd:string . }";

        log.debug("Executing query: ");
        log.debug("Query: \n{}", query);

        countResults(76, query);
    }
}

