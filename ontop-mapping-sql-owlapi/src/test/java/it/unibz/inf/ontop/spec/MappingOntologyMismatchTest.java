package it.unibz.inf.ontop.spec;

import it.unibz.inf.ontop.exception.MappingOntologyMismatchException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllOWLAPIConfiguration;
import org.junit.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Checks that the correct exceptions are thrown in case of a mismatch between the T-Box
 * and the mapping
 */
public class MappingOntologyMismatchTest {

    private static Connection CONNECTION;
    private static final String JDBC_URL = "jdbc:h2:mem:mapping-onto-mismatch";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static final String DIR = "/mismatch/";
    private static final String CREATE_SCRIPT = DIR + "create-db.sql";
    private static final String DROP_SCRIPT = DIR + "drop-db.sql";
    private static final String DEFAULT_OWL_FILE = DIR + "marriage.ttl";

    @Test
    public void testValidUsage() throws OBDASpecificationException {
        extractSpecification(DEFAULT_OWL_FILE, DIR +"marriage_valid.obda");
    }

    @Ignore("TODO: support it")
    @Test(expected = MappingOntologyMismatchException.class)
    public void testAbusiveTypedDataPropertyUsageInsteadOfObject() throws OBDASpecificationException {
        extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_abusive_typed_data_prop.obda");
    }

    @Ignore("TODO: support it")
    @Test(expected = MappingOntologyMismatchException.class)
    public void testAbusiveTypedDataPropertyUsageInsteadOfClass() throws OBDASpecificationException {
        extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_abusive_typed_data_prop2.obda");
    }

    @Ignore("TODO: support it")
    @Test(expected = MappingOntologyMismatchException.class)
    public void testAbusiveUntypedDataPropertyUsageInsteadOfObject() throws OBDASpecificationException {
        extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_abusive_untyped_data_prop.obda");
    }

    @Ignore("TODO: support it")
    @Test(expected = MappingOntologyMismatchException.class)
    public void testAbusiveObjectPropertyUsageInsteadOfData1() throws OBDASpecificationException {
        extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_abusive_object_prop1.obda");
    }

    @Ignore("TODO: support it")
    @Test(expected = MappingOntologyMismatchException.class)
    public void testAbusiveObjectPropertyUsageInsteadOfData2() throws OBDASpecificationException {
        extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_abusive_object_prop2.obda");
    }

    @Ignore("TODO: support it")
    @Test(expected = MappingOntologyMismatchException.class)
    public void testAbusiveObjectPropertyUsageInsteadOfClass() throws OBDASpecificationException {
        extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_abusive_object_prop3.obda");
    }

    @Ignore("TODO: support it")
    @Test(expected = MappingOntologyMismatchException.class)
    public void testAbusiveClass() throws OBDASpecificationException {
        extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_abusive_class.obda");
    }

    @Ignore("TODO: support it")
    @Test(expected = MappingOntologyMismatchException.class)
    public void testWrongDatatype() throws OBDASpecificationException {
        extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_wrong_datatype.obda");
    }


    private OBDASpecification extractSpecification(String owlFile, String obdaFile) throws OBDASpecificationException {
        OntopMappingSQLAllOWLAPIConfiguration configuration = generateConfiguration(owlFile, obdaFile);
        return configuration.loadSpecification();
    }


    @BeforeClass
    public static void setUp() throws Exception {
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
        CONNECTION = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
        Statement st = CONNECTION.createStatement();

        FileReader reader = new FileReader(MappingOntologyMismatchTest.class.getResource(CREATE_SCRIPT).getFile());
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        CONNECTION.commit();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        dropTables();
        CONNECTION.close();
    }

    private static void dropTables() throws SQLException, IOException {

        Statement st = CONNECTION.createStatement();

        FileReader reader = new FileReader(MappingOntologyMismatchTest.class.getResource(DROP_SCRIPT).getFile());
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        st.close();
        CONNECTION.commit();
    }

    OntopMappingSQLAllOWLAPIConfiguration generateConfiguration(String owlFile, String obdaFile) {
        Class<? extends MappingOntologyMismatchTest> klass = this.getClass();

        return OntopMappingSQLAllOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(klass.getResource(owlFile).getFile())
                .nativeOntopMappingFile(klass.getResource(obdaFile).getFile())
                .jdbcUrl(JDBC_URL)
                .jdbcUser(DB_USER)
                .jdbcPassword(DB_PASSWORD)
                .build();
    }

}
