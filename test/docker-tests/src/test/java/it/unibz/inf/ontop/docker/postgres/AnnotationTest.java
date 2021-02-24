package it.unibz.inf.ontop.docker.postgres;


import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;

/**
 * Class to test if annotation property can be treated as data property and object property
 *
 *
 */
@Ignore("Too slow (20 min)!")
public class AnnotationTest extends AbstractVirtualModeTest {

    Logger log = LoggerFactory.getLogger(this.getClass());

    final static String owlFile = "/pgsql/annotation/doid.owl";
    final static String obdaFile = "/pgsql/annotation/doid.obda";
    final static String propertyFile = "/pgsql/annotation/doid.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertyFile);
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

