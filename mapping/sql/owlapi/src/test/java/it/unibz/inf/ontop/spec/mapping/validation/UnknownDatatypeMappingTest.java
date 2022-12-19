package it.unibz.inf.ontop.spec.mapping.validation;

import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.OBDASpecification;

import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static it.unibz.inf.ontop.spec.mapping.validation.TestConnectionManager.getDatatype;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UnknownDatatypeMappingTest {

    private static final String JDBC_URL = "jdbc:h2:mem:unknown-datatype-inference";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static final String DIR = "/unknown-datatype/";
    private static final String CREATE_SCRIPT = DIR + "create-db.sql";
    private static final String DROP_SCRIPT = DIR + "drop-db.sql";
    private static final String DEFAULT_OWL_FILE = DIR + "marriage.ttl";
    private static TestConnectionManager TEST_MANAGER;

    @Test
    public void testUpperFunction() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_unknown_function.obda");
        assertEquals(Optional.of(XSD.STRING), getDatatype(spec.getSaturatedMapping()));
    }

    @Test
    public void testMappingUnknownStringFunction() throws OBDASpecificationException {
        assertThrows(UnknownDatatypeException.class,
                () -> TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_view_function.obda"));
    }

    @Test
    public void testMappingFunction() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_function.obda");
        assertEquals(Optional.of(XSD.STRING), getDatatype(spec.getSaturatedMapping()));
    }

    @Test
    public void testMappingTargetFunction() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_target_function.obda");
        assertEquals(Optional.of(XSD.STRING), getDatatype(spec.getSaturatedMapping()));
    }

    @Test
    public void testMappingIntFunction() throws OBDASpecificationException {
        OBDASpecification spec =TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_int_function.obda");
        assertEquals(Optional.of(XSD.STRING), getDatatype(spec.getSaturatedMapping()));
    }

    @Test
    public void testMappingToCharFunction() {
        assertThrows(UnknownDatatypeException.class,
                () -> TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_tochar_function.obda"));
    }

    @Test
    public void testMappingRDFSLiteralFunction() {
        assertThrows(UnknownDatatypeException.class,
                () -> TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_rdfsliteral.obda"));
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
