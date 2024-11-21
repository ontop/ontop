package it.unibz.inf.ontop.cli;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;


public class OntopEndpointWithAccessControlInLensTest extends AbstractOntopEndpointWithAccessControlTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();
    private static final String PORT = "29890";
    private static final String DBURL = "jdbc:h2:tcp://localhost:19123/./src/test/resources/h2/books;ACCESS_MODE_DATA=r";
    private static final String DBUSER = "sa";
    private static final String DBPASSWORD = "test";

    public OntopEndpointWithAccessControlInLensTest() {
        super(PORT);
    }


    @BeforeClass
    public static void setupEndpoint() {
        Ontop.main("endpoint", "-m", "src/test/resources/books/exampleBooks-access-control-in-lens.obda",
                "-p", "src/test/resources/books/exampleBooks-access-control.properties",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-l", "src/test/resources/books/exampleBooks-access-control-lenses.json",
                "--db-url=" + DBURL,
                "--db-user=" + DBUSER,
                "--db-password=" + DBPASSWORD,
                "--port=" + PORT);
    }


}
