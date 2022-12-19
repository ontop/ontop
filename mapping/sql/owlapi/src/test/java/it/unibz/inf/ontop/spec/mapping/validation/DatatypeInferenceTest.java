package it.unibz.inf.ontop.spec.mapping.validation;

import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.OBDASpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.spec.mapping.validation.TestConnectionManager.getDatatype;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class DatatypeInferenceTest {

    private static final String JDBC_URL = "jdbc:h2:mem:mapping-datatype-inference";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static final String DIR = "/datatype-inference/";
    private static final String CREATE_SCRIPT = DIR + "create-db.sql";
    private static final String DROP_SCRIPT = DIR + "drop-db.sql";
    private static final String DEFAULT_OWL_FILE = DIR + "marriage.ttl";
    private static TestConnectionManager TEST_MANAGER;


    @Test
    public void testMappingOntologyConflict() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_invalid_datatype.obda"));
    }

    @Test
    public void testRangeInferredDatatype() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE,
                DIR + "marriage_range_datatype.obda");
        assertEquals(Optional.of(XSD.STRING), getDatatype(spec.getSaturatedMapping()));
    }

    @Test
    public void testNoRangeMappingDatatype() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE,
                DIR + "marriage_no_range_prop_mapping_datatype.obda");
        assertEquals(Optional.of(XSD.INTEGER), getDatatype(spec.getSaturatedMapping()));
    }

    @Test
    public void testNoRangeColtype() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE,
                DIR + "marriage_no_range_prop_coltype.obda");
        assertEquals(Optional.of(XSD.STRING), getDatatype(spec.getSaturatedMapping()));
    }

    @Test
    public void testUnknownMappingDatatype() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE,
                DIR + "marriage_unknown_prop_mapping_datatype.obda");
        assertEquals(Optional.of(XSD.INTEGER), getDatatype(spec.getSaturatedMapping()));
    }

    @Test
    public void testUnknownStringColtype() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE,
                DIR + "marriage_unknown_prop_coltype.obda");
        assertEquals(Optional.of(XSD.STRING), getDatatype(spec.getSaturatedMapping()));
    }

    @Test
    public void testUnknownIntegerColtype() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE,
                DIR + "marriage_unknown_prop_coltype_int.obda");
        assertEquals(Optional.of(XSD.INTEGER), getDatatype(spec.getSaturatedMapping()));
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
