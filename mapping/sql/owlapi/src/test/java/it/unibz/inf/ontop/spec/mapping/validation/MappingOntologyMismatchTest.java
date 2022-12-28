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

/**
 * Checks that the correct exceptions are thrown in case of a mismatch between the TBox
 * and the mapping
 */
public class MappingOntologyMismatchTest {

    private static final String JDBC_URL = "jdbc:h2:mem:mapping-onto-mismatch";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static final String DIR = "/datatype-inference/";
    private static final String CREATE_SCRIPT = DIR + "create-db.sql";
    private static final String DROP_SCRIPT = DIR + "drop-db.sql";
    private static final String DEFAULT_OWL_FILE = DIR + "marriage.ttl";
    private static final String SELECT_QUERY = "SELECT * FROM \"person\"";
    private static TestConnectionManager TEST_MANAGER;

    @Test
    public void testValidUsageObjectProperty() throws OBDASpecificationException, TargetQueryParserException {
        Optional<IRI> datatype = TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                "<http://example.com/person/{id}> :hasSpouse <http://example.com/person/{spouse}> .");
        assertEquals(Optional.empty(), datatype);
    }

    @Test
    public void testValidUsageDataProperty() throws OBDASpecificationException, TargetQueryParserException {
        Optional<IRI> datatype = TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                "<http://example.com/person/{id}> :firstName {first_name}^^xsd:string .");
        assertEquals(Optional.of(XSD.STRING), datatype);
    }

    @Test
    public void testValidUsageClass() throws OBDASpecificationException, TargetQueryParserException {
        Optional<IRI> datatype = TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                "<http://example.com/person/{id}> a :Person .");
        assertEquals(Optional.empty(),datatype);
    }

    @Test
    public void testAbusiveTypedDataPropertyUsageInsteadOfObject() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                        "<http://example.com/person/{id}> :hasSpouse \"{spouse}\"^^xsd:string ."));
    }

    @Test
    public void testAbusiveTypedDataPropertyUsageInsteadOfClass() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                "<http://example.com/person/{id}> :Person \"{spouse}\"^^xsd:string ."));
    }

    @Test
    public void testAbusiveUntypedDataPropertyUsageInsteadOfObject() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                "<http://example.com/person/{id}> :hasSpouse {spouse} ."));
    }

    @Test
    public void testAbusiveObjectPropertyUsageInsteadOfData1() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                        "<http://example.com/person/{id}> :firstName <{first_name}> ."));
    }

    @Test
    public void testAbusiveObjectPropertyUsageInsteadOfData2() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                        "<http://example.com/person/{id}> :firstName <http://localhost/{first_name}> ."));
    }

    @Test
    public void testAbusiveObjectPropertyUsageInsteadOfClass() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                        "<http://example.com/person/{id}> :Person <http://example.com/person/{spouse}> ."));
    }

    @Test
    public void testAbusiveClass() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                        "<http://example.com/person/{id}> a :firstName ."));
    }

    @Test
    public void testWrongDatatype1() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                        "<http://example.com/person/{id}> :firstName {first_name}^^xsd:integer ."));
    }

    @Test
    public void testWrongDatatype2() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                        "<http://example.com/person/{id}> :age {age}^^xsd:string ."));
    }

    @Test
    public void testTooGenericDatatype() {
        assertThrows(MappingOntologyMismatchException.class,
                () -> TEST_MANAGER.getMappingObjectDatatype(DEFAULT_OWL_FILE, SELECT_QUERY,
                        "<http://example.com/person/{id}> :specializedAge \"{age}\"^^xsd:integer ."));
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
