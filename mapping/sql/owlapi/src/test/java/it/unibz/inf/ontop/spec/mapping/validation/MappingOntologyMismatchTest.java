package it.unibz.inf.ontop.spec.mapping.validation;

import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks that the correct exceptions are thrown in case of a mismatch between the T-Box
 * and the mapping
 */
public class MappingOntologyMismatchTest {

    private static final String JDBC_URL = "jdbc:h2:mem:mapping-onto-mismatch";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static final String DIR = "/mismatch/";
    private static final String CREATE_SCRIPT = DIR + "create-db.sql";
    private static final String DROP_SCRIPT = DIR + "drop-db.sql";
    private static final String DEFAULT_OWL_FILE = DIR + "marriage.ttl";
    private static TestConnectionManager TEST_MANAGER;

    @Test
    public void testValidUsage() throws OBDASpecificationException {
        TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR +"marriage_valid.obda");
    }

    @Test
    public void testAbusiveTypedDataPropertyUsageInsteadOfObject() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_abusive_typed_data_prop.obda"));
    }

    @Test
    public void testAbusiveTypedDataPropertyUsageInsteadOfClass() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_abusive_typed_data_prop2.obda"));
    }

    @Test
    public void testAbusiveUntypedDataPropertyUsageInsteadOfObject() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_abusive_untyped_data_prop.obda"));
    }

    @Test
    public void testAbusiveObjectPropertyUsageInsteadOfData1() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_abusive_object_prop1.obda"));
    }

    @Test
    public void testAbusiveObjectPropertyUsageInsteadOfData2() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_abusive_object_prop2.obda"));
    }

    @Test
    public void testAbusiveObjectPropertyUsageInsteadOfClass() throws OBDASpecificationException {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_abusive_object_prop3.obda"));
    }

    @Test
    public void testAbusiveClass() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_abusive_class.obda"));
    }

    @Test
    public void testWrongDatatype1() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_wrong_datatype.obda"));
    }

    @Test
    public void testWrongDatatype2() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_wrong_datatype2.obda"));
    }

    @Test
    public void testTooGenericDatatype() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_too_generic_datatype_in_mapping.obda"));
    }

    @BeforeAll
    public static void setUp() throws Exception {
        TEST_MANAGER = new TestConnectionManager(JDBC_URL, DB_USER, DB_PASSWORD, CREATE_SCRIPT, DROP_SCRIPT);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        TEST_MANAGER.close();
    }

}
