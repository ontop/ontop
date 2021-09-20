package it.unibz.inf.ontop.docker;

import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

public class BootstrapTest extends AbstractBootstrapTest {
    private static final String baseIRI = "http://h2-bootstrap-test";
    private static final String owlOutputFile = "src/test/resources/h2/bootstrap/output.owl";
    private static final String obdaOutputFile = "src/test/resources/h2/bootstrap/output.obda";

    private final String propertyFile = this.getClass().getResource("/h2/bootstrap/bootstrap.properties").toString();
    private static final String sqlCreateFile = "src/test/resources/h2/bootstrap/create.sql";
    private static final String sqlDropFile = "src/test/resources//h2/bootstrap/drop.sql";

    @Before
    public void setUp() throws Exception {
        createTables(sqlCreateFile, "jdbc:h2:mem:questjunitdb", "sa", "");
    }

    @After
    public void tearDown() throws Exception {
        dropTables(sqlDropFile);
    }

    @Test
    public void testBootstrap() throws Exception {
        bootstrap(propertyFile, baseIRI, owlOutputFile, obdaOutputFile);
        OWLStatement st = loadGeneratedFiles(owlOutputFile, obdaOutputFile, propertyFile);
        st.executeSelectQuery("SELECT * WHERE { ?x <http://h2-bootstrap-test/course-registration#ref-COURSE_ID> ?y }");
    }
}
