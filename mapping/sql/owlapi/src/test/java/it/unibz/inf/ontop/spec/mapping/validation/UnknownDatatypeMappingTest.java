package it.unibz.inf.ontop.spec.mapping.validation;

import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class UnknownDatatypeMappingTest {

    private static final String JDBC_URL = "jdbc:h2:mem:unknown-datatype-inference";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static final String DIR = "/unknown-datatype/";
    private static final String CREATE_SCRIPT = DIR + "create-db.sql";
    private static final String DROP_SCRIPT = DIR + "drop-db.sql";
    private static final String DEFAULT_OWL_FILE = DIR + "marriage.ttl";
    private static TestConnectionManager TEST_MANAGER;

    @BeforeClass
    public static void setUp() throws Exception {
        TEST_MANAGER = new TestConnectionManager(JDBC_URL, DB_USER, DB_PASSWORD, CREATE_SCRIPT, DROP_SCRIPT);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        TEST_MANAGER.close();
    }

    @Test(expected = UnknownDatatypeException.class)
    public void testFailing() throws OBDASpecificationException {
        TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_unknown_function.obda");
    }

    @Test(expected = UnknownDatatypeException.class)
    public void testMappingUnknownStringFunction() throws OBDASpecificationException {
        TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_unknown_function2.obda");
    }

    @Test(expected = UnknownDatatypeException.class)
    public void testMappingFunction() throws OBDASpecificationException {
        TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_function.obda");
    }
}
