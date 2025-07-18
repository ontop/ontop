package it.unibz.inf.ontop.docker.lightweight.dremio.other;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.exception.OntopInvalidKGQueryException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.query.KGQueryFactory;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

public class StringTypesAliasingDremioTest {
    private static final String OBDA_FILE = "/dremioTypes/mapping.obda";
    private static final String ONTOLOGY_FILE = "/dremioTypes/ontology.ttl";
    private static final String PROPERTIES_FILE = "/dremioTypes/prop.properties";
    private static final String LENSES_FILE = "/dremioTypes/lenses.json";
    private static final String DB_METADATA_FILE = "/dremioTypes/db-metadata.json";

    private static OntopVirtualRepository repository;
    private static KGQueryFactory kgQueryFactory;

    @BeforeAll
    public static void setUp() {
        initOBDA(OBDA_FILE, ONTOLOGY_FILE, PROPERTIES_FILE, LENSES_FILE, DB_METADATA_FILE);
    }

    @Test
    public void testStringTypesCastSimplification() {
        String sparqlQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX eslo: <http://eia-demo.metaphacts.cloud/ontology/>\n" +
                "SELECT * WHERE {\n" +
                "   ?emp eslo:worksAtDepartment ?dep .\n" +
                " MINUS {\n" +
                "   ?emp a eslo:Employee .\n" +
                " }\n" +
                "} \n";

        String finalQuery = null;
        try {
            finalQuery = reformulate(sparqlQuery);
        } catch (OntopUnsupportedKGQueryException | OntopInvalidKGQueryException | OntopReformulationException e) {
            Assertions.fail("Reformulation failed: " + e.getMessage());
        }

        String expectedFinalQuery = "ans1(emp, dep)\n" +
                "EMPTY [emp, dep]";
        Assertions.assertEquals(expectedFinalQuery, finalQuery);
    }

    private static void initOBDA(String obdaRelativePath, @Nullable String ontologyRelativePath,
                                 String propertyFile, @Nullable String lensesFile,
                                 @Nullable String dbMetadataFile) {

        String propertyFilePath = StringTypesAliasingDremioTest.class.getResource(propertyFile).getPath();

        OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(StringTypesAliasingDremioTest.class.getResource(obdaRelativePath).getPath())
                .propertyFile(propertyFilePath)
                .enableTestMode();

        if (ontologyRelativePath != null)
            builder.ontologyFile(StringTypesAliasingDremioTest.class.getResource(ontologyRelativePath).getPath());

        builder.propertyFile(StringTypesAliasingDremioTest.class.getResource(propertyFile).getPath());

        if (lensesFile != null)
            builder.lensesFile(StringTypesAliasingDremioTest.class.getResource(lensesFile).getPath());

        if (dbMetadataFile != null)
            builder.dbMetadataFile(StringTypesAliasingDremioTest.class.getResource(dbMetadataFile).getPath());

        OntopSQLOWLAPIConfiguration config = builder.build();

        OntopVirtualRepository repo = OntopRepository.defaultRepository(config);
        repo.init();

        repository = repo;
        kgQueryFactory = config.getKGQueryFactory();

    }

    private String reformulate(String query) throws OntopUnsupportedKGQueryException, OntopInvalidKGQueryException, OntopReformulationException {
        QueryReformulator reformulator = repository.getOntopEngine().getQueryReformulator();
        return reformulator.reformulateIntoNativeQuery(kgQueryFactory.createSPARQLQuery(query),
                        reformulator.getQueryContextFactory().create(ImmutableMap.of()),
                        reformulator.getQueryLoggerFactory().create(ImmutableMap.of()))
                .toString();
    }
}
