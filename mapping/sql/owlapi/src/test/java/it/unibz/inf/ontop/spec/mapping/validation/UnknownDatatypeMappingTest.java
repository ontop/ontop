package it.unibz.inf.ontop.spec.mapping.validation;

import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
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
    private static final String DIR = "/datatype-inference/";
    private static final String CREATE_SCRIPT = DIR + "create-db.sql";
    private static final String DROP_SCRIPT = DIR + "drop-db.sql";
    private static final String DEFAULT_OWL_FILE = DIR + "marriage.ttl";

    private static TestConnectionManager TEST_MANAGER;


    @Test
    public void testUpperFunction() throws OBDASpecificationException, TargetQueryParserException {
        OBDASpecification spec = TEST_MANAGER.loadSpecification(DEFAULT_OWL_FILE,
                "SELECT \"id\", UPPER(\"first_name\") as \"first_name\"  FROM \"person\"",
                "<http://example.com/person/{id}> :firstName {first_name} .");
        assertEquals(Optional.of(XSD.STRING), getDatatype(spec.getSaturatedMapping()));
    }

    @Test
    public void testMappingUnknownStringFunction() {
        assertThrows(UnknownDatatypeException.class,
                () -> TEST_MANAGER.loadSpecification(DEFAULT_OWL_FILE,
                        "SELECT \"id\", LTRIM(\"first_name\") as \"first_name\"  FROM \"person\"",
                    "<http://example.com/person/{id}> :firstName {first_name} ."));
    }

    @Test
    public void testMappingFunction() throws OBDASpecificationException, TargetQueryParserException {
        OBDASpecification spec = TEST_MANAGER.loadSpecification(DEFAULT_OWL_FILE,
                "SELECT \"id\", CONCAT(\"first_name\", \"last_name\" ) as \"name\"  FROM \"person\"",
                "<http://example.com/person/{id}> :firstName {name} .");
        assertEquals(Optional.of(XSD.STRING), getDatatype(spec.getSaturatedMapping()));
    }

    @Test
    public void testMappingTargetFunction() throws OBDASpecificationException, TargetQueryParserException {
        OBDASpecification spec = TEST_MANAGER.loadSpecification(DEFAULT_OWL_FILE,
                "SELECT \"id\", \"first_name\", \"last_name\"  FROM \"person\"",
                "<http://example.com/person/{id}> :firstName \"{first_name}||{last_name}\" .");
        assertEquals(Optional.of(XSD.STRING), getDatatype(spec.getSaturatedMapping()));
    }

    @Test
    public void testMappingIntFunction() throws OBDASpecificationException, TargetQueryParserException {
        OBDASpecification spec = TEST_MANAGER.loadSpecification(DEFAULT_OWL_FILE,
                "SELECT \"id\", CONCAT(\"spouse\", \"id\" ) as \"nameCode\"  FROM \"person\"",
                "<http://example.com/person/{id}> :untypedName {nameCode} .");
        assertEquals(Optional.of(XSD.STRING), getDatatype(spec.getSaturatedMapping()));
    }

    @Test
    public void testMappingToCharFunction() {
        assertThrows(UnknownDatatypeException.class,
                () -> TEST_MANAGER.loadSpecification(DEFAULT_OWL_FILE,
                        "SELECT \"id\", TO_CHAR(\"spouse\") as \"name\"  FROM \"person\"",
                        "<http://example.com/person/{id}> :firstName {name} ."));
    }

    @Test
    public void testMappingRDFSLiteralFunction() {
        assertThrows(UnknownDatatypeException.class,
                () -> TEST_MANAGER.loadSpecification(DEFAULT_OWL_FILE,
                        "SELECT \"id\", LTRIM(\"first_name\") as \"first_name\"  FROM \"person\"",
                        "<http://example.com/person/{id}> :untypedName \"{first_name}\"^^rdfs:Literal ."));
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
