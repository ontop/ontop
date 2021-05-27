package it.unibz.inf.ontop.docker.db2;

import it.unibz.inf.ontop.docker.AbstractBootstrapTest;
import org.junit.Test;

public class DB2BootstrapTest extends AbstractBootstrapTest {

    static private final String baseIRI = "http://db2-bootstrap-test";
    static private final String owlOutputFile = "src/test/resources/db2/bootstrap/output.owl";
    static private final String obdaOutputFile = "src/test/resources/db2/bootstrap/output.obda";

    private final String propertyFile = this.getClass().getResource("/db2/db2-stock.properties").toString();

    @Test
    public void testBootstrap() throws Exception {
        bootstrap(propertyFile, baseIRI, owlOutputFile, obdaOutputFile);
        loadGeneratedFiles(owlOutputFile, obdaOutputFile, propertyFile);
    }
}
