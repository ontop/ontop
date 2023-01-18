package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;


public class NestedConcatTest extends AbstractVirtualModeTest {

    private static final String owlfile = "/mysql/nestedconcat/test.owl";
    private static final String obdafile = "/mysql/nestedconcat/test.obda";
    private static final String propertyfile = "/mysql/nestedconcat/test.properties";

    private static EngineConnection CONNECTION;

    @BeforeClass
    public static void before() {
        CONNECTION = createReasoner(owlfile, obdafile, propertyfile);
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
    public void testConcat() throws Exception {
        countResults(1, "PREFIX : <http://www.semanticweb.org/meme/ontologies/2015/3/test#>\n" +
                "SELECT ?per ?yS ?yE\n" +
                "WHERE{\n" +
                "?per a :Period ; :yStart ?yS ; :yEnd ?yE\n" +
                "}\n" +
                "LIMIT 1");
    }
}
