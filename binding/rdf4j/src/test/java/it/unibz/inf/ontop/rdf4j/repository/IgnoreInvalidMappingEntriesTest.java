package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.ImmutableMetadata;
import it.unibz.inf.ontop.dbschema.Lens;
import it.unibz.inf.ontop.dbschema.LensMetadataProvider;
import it.unibz.inf.ontop.dbschema.SerializedMetadataProvider;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;


public class IgnoreInvalidMappingEntriesTest extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/ignore-invalid-mapping-entries/test.obda";
    private static final String SQL_SCRIPT = "/ignore-invalid-mapping-entries/test.sql";
    private static final String PROPERTY_FILE = "/ignore-invalid-mapping-entries/ignore-invalid.properties";
    private static final String LENS_FILE = "/ignore-invalid-mapping-entries/lenses.json";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, PROPERTY_FILE, LENS_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    @Test
    public void testCorrectNumberOfIndividuals() {
        String query = "PREFIX : <http://www.ontop-vkg.com/ignore-invalid-test#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Animal . \n" +
                "}";
        assertEquals(8, runQueryAndCount(query));
    }

    @Test
    public void testCorrectNumberOfPartiallyIncorrectIndividuals() {
        String query = "PREFIX : <http://www.ontop-vkg.com/ignore-invalid-test#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Rabbit . \n" +
                "}";
        assertEquals(2, runQueryAndCount(query));
    }

    @Test
    public void testCorrectNumberOfFullyIncorrectIndividuals() {
        String query = "PREFIX : <http://www.ontop-vkg.com/ignore-invalid-test#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Turtle . \n" +
                "}";
        assertEquals(0, runQueryAndCount(query));
    }

    @Test
    public void testAccessNonExistentTable() {
        String query = "PREFIX : <http://www.ontop-vkg.com/ignore-invalid-test#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Bird . \n" +
                "}";
        assertEquals(0, runQueryAndCount(query));
    }

    @Test
    public void testAccessNonExistentLensColumn() {
        String query = "PREFIX : <http://www.ontop-vkg.com/ignore-invalid-test#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?d a :Dog1 . \n" +
                " ?d :colour ?v . \n" +
                "}";
        assertEquals(0, runQueryAndCount(query));
    }

    @Test
    public void testAccessInvalidLens() {
        String query = "PREFIX : <http://www.ontop-vkg.com/ignore-invalid-test#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?v a :Dog2 . \n" +
                "}";
        assertEquals(0, runQueryAndCount(query));
    }

    @Test
    public void testInvalidLensNonExistentColumn() throws Exception {
        String lens = "/ignore-invalid-mapping-entries/lenses-column.json";
        String dbExtract = "/ignore-invalid-mapping-entries/test.db-extract.json";
        ImmutableSet<Lens> lenses = loadLensesH2(AbstractRDF4JTest.class.getResource(lens).getPath(),
                AbstractRDF4JTest.class.getResource(dbExtract).getPath());
        assertEquals(7, lenses.size());
    }

    @Test
    public void testInvalidLensNonExistentTable() throws Exception {
        String lens = "/ignore-invalid-mapping-entries/lenses-table.json";
        String dbExtract = "/ignore-invalid-mapping-entries/test.db-extract.json";
        ImmutableSet<Lens> lenses = loadLensesH2(AbstractRDF4JTest.class.getResource(lens).getPath(),
                AbstractRDF4JTest.class.getResource(dbExtract).getPath());
        assertEquals(2, lenses.size());
    }

    public static ImmutableSet<Lens> loadLensesH2(String viewFilePath,
                                                  String dbMetadataFilePath)
            throws Exception {

        return loadViewDefinitions(
                viewFilePath,
                dbMetadataFilePath,
                OntopSQLCoreConfiguration.defaultBuilder()
                        .jdbcUrl("jdbc:h2:mem:nowhere")
                        .jdbcDriver("org.h2.Driver")
                        .propertyFile(AbstractRDF4JTest.class.getResource(PROPERTY_FILE).getPath())
                        .build()
        );
    }

    private static ImmutableSet<Lens> loadViewDefinitions(String viewFilePath, String dbMetadataFilePath, OntopSQLCoreConfiguration configuration) throws Exception {
        Injector injector = configuration.getInjector();
        SerializedMetadataProvider.Factory serializedMetadataProviderFactory = injector.getInstance(SerializedMetadataProvider.Factory.class);
        LensMetadataProvider.Factory viewMetadataProviderFactory = injector.getInstance(LensMetadataProvider.Factory.class);

        SerializedMetadataProvider dbMetadataProvider;
        try (Reader dbMetadataReader = new FileReader(dbMetadataFilePath)) {
            dbMetadataProvider = serializedMetadataProviderFactory.getMetadataProvider(dbMetadataReader);
        }

        LensMetadataProvider viewMetadataProvider;
        try (Reader viewReader = new FileReader(viewFilePath)) {
            viewMetadataProvider = viewMetadataProviderFactory.getMetadataProvider(dbMetadataProvider, viewReader);
        }

        ImmutableMetadata metadata = ImmutableMetadata.extractImmutableMetadata(viewMetadataProvider);

        return metadata.getAllRelations().stream()
                .filter(r -> r instanceof Lens)
                .map(r -> (Lens) r)
                .collect(ImmutableCollectors.toSet());

    }
}
