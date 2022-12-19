package it.unibz.inf.ontop.spec.mapping.validation;

import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.apache.commons.rdf.api.IRI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

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
    private static final String SELECT_QUERY = "SELECT * FROM \"person\"";
    private static TestConnectionManager TEST_MANAGER;


    @Test
    public void testMappingOntologyConflict() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                        "<http://example.com/person/{id}> :firstName {first_name}^^xsd:integer ."));
    }

    @Test
    public void testRangeInferredDatatype() throws OBDASpecificationException, TargetQueryParserException {
        Optional<IRI> datatype = TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                "<http://example.com/person/{id}> :firstName {first_name} .");
        assertEquals(Optional.of(XSD.STRING), datatype);
    }

    @Test
    public void testNoRangeMappingDatatype() throws OBDASpecificationException, TargetQueryParserException {
        Optional<IRI> datatype = TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                "<http://example.com/person/{id}> :untypedName {first_name}^^xsd:integer .");
        assertEquals(Optional.of(XSD.INTEGER), datatype);
    }

    @Test
    public void testNoRangeColtype() throws OBDASpecificationException, TargetQueryParserException {
        Optional<IRI> datatype = TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                "<http://example.com/person/{id}> :untypedName {first_name} .");
        assertEquals(Optional.of(XSD.STRING), datatype);
    }

    @Test
    public void testUnknownMappingDatatype() throws OBDASpecificationException, TargetQueryParserException {
        Optional<IRI> datatype = TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                "<http://example.com/person/{id}> :someName {first_name}^^xsd:integer .");
        assertEquals(Optional.of(XSD.INTEGER), datatype);
    }

    @Test
    public void testUnknownStringColtype() throws OBDASpecificationException, TargetQueryParserException {
        Optional<IRI> datatype = TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                "<http://example.com/person/{id}> :someName {first_name} .");
        assertEquals(Optional.of(XSD.STRING), datatype);
    }

    @Test
    public void testUnknownIntegerColtype() throws OBDASpecificationException, TargetQueryParserException {
        Optional<IRI> datatype = TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                "<http://example.com/person/{id}> :someInteger {id} .");
        assertEquals(Optional.of(XSD.INTEGER), datatype);
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
