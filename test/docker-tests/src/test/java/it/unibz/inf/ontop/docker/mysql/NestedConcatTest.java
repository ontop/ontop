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


public class NestedConcatTest extends AbstractVirtualModeTest {

    private static final String owlfile = "/mysql/nestedconcat/test.owl";
    private static final String obdafile = "/mysql/nestedconcat/test.obda";
    private static final String propertyfile = "/mysql/nestedconcat/test.properties";

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
    public void testConcat() throws Exception {
        countResults(1, "PREFIX : <http://www.semanticweb.org/meme/ontologies/2015/3/test#>\n" +
                "SELECT ?per ?yS ?yE\n" +
                "WHERE{\n" +
                "?per a :Period ; :yStart ?yS ; :yEnd ?yE\n" +
                "}\n" +
                "LIMIT 1");
    }
}
