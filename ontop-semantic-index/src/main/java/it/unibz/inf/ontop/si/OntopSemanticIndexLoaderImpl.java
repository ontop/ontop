package it.unibz.inf.ontop.si;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.MappingFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDAModelImpl;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlapi.OWLAPIABoxIterator;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

/**
 * TODO: find a better name
 */
public class OntopSemanticIndexLoaderImpl implements OntopSemanticIndexLoader {

    private static final Logger log = LoggerFactory.getLogger(OntopSemanticIndexLoaderImpl.class);

    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";
    private static final boolean OPTIMIZE_EQUIVALENCES = true;;
    private final QuestConfiguration configuration;
    private final Connection connection;


    private OntopSemanticIndexLoaderImpl(QuestConfiguration configuration, Connection connection) {
        this.configuration = configuration;
        this.connection = connection;
    }

    @Override
    public QuestConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void close() {
        try {
            if (connection != null && (!connection.isClosed())) {
                connection.close();
            }
        } catch (SQLException e) {
            log.error("Error while closing the DB: " + e.getMessage());
        }
    }


    public static OntopSemanticIndexLoader loadOntologyIndividuals(String owlFile, Properties properties)
            throws SemanticIndexException {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlFile));

            return loadOntologyIndividuals(ontology, properties);

        } catch (OWLOntologyCreationException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }


    /**
     * TODO: find a better name
     */
    public static OntopSemanticIndexLoader loadOntologyIndividuals(OWLOntology owlOntology, Properties properties)
            throws SemanticIndexException {

        Set<OWLOntology> ontologyClosure = owlOntology.getOWLOntologyManager().getImportsClosure(owlOntology);

        Ontology ontology = OWLAPITranslatorUtility.mergeTranslateOntologies(ontologyClosure);
        ImmutableOntologyVocabulary vocabulary = ontology.getVocabulary();

        final TBoxReasoner reformulationReasoner = TBoxReasonerImpl.create(ontology, OPTIMIZE_EQUIVALENCES);

        RDBMSSIRepositoryManager dataRepository = new RDBMSSIRepositoryManager(reformulationReasoner, vocabulary);

        log.warn("Semantic index mode initializing: \nString operation over URI are not supported in this mode ");
        // we work in memory (with H2), the database is clean and
        // Quest will insert new Abox assertions into the database.
        dataRepository.generateMetadata();

        String jdbcUrl = buildNewJdbcUrl();
        try {
            Connection localConnection = DriverManager.getConnection(jdbcUrl, DEFAULT_USER, DEFAULT_PASSWORD);

            // Creating the ABox repository
            dataRepository.createDBSchemaAndInsertMetadata(localConnection);

            OWLAPIABoxIterator aBoxIter = new OWLAPIABoxIterator(ontologyClosure, vocabulary);

            int count = dataRepository.insertData(localConnection, aBoxIter, 5000, 500);
            log.debug("Inserted {} triples from the ontology.", count);

            QuestConfiguration configuration = createConfiguration(dataRepository, owlOntology, jdbcUrl, properties);
            return new OntopSemanticIndexLoaderImpl(configuration, localConnection);

        } catch (SQLException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }

    private static QuestConfiguration createConfiguration(RDBMSSIRepositoryManager dataRepository, OWLOntology owlOntology,
                                                          String jdbcUrl, Properties properties) throws SemanticIndexException {
        OBDAModel ppMapping = createPPMapping(dataRepository);

        /**
         * Tbox: ontology without the ABox axioms (are in the DB now).
         */
        OWLOntologyManager newManager = OWLManager.createOWLOntologyManager();
        OWLOntology tbox;
        try {
            tbox = newManager.copyOntology(owlOntology, OntologyCopy.SHALLOW);
        } catch (OWLOntologyCreationException e) {
            throw new SemanticIndexException(e.getMessage());
        }
        newManager.removeAxioms(tbox, tbox.getABoxAxioms(Imports.EXCLUDED));

        return QuestConfiguration.defaultBuilder()
                .obdaModel(ppMapping)
                .ontology(tbox)
                .properties(properties)
                .jdbcUrl(jdbcUrl)
                .jdbcUser(DEFAULT_USER)
                .jdbcPassword(DEFAULT_PASSWORD)
                .iriDictionary(dataRepository.getUriMap())
                .build();
    }


    private static OBDAModel createPPMapping(RDBMSSIRepositoryManager dataRepository) {
        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .build();
        MappingFactory mappingFactory = defaultConfiguration.getInjector().getInstance(MappingFactory.class);
        PrefixManager prefixManager = mappingFactory.create(ImmutableMap.of());

        try {
            return new OBDAModelImpl(dataRepository.getMappings(),
                    mappingFactory.create(prefixManager));

        } catch (DuplicateMappingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private static String buildNewJdbcUrl() {
        return "jdbc:h2:mem:questrepository:" + System.currentTimeMillis() + ";LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0";
    }


    public static OntopSemanticIndexLoader loadVirtualAbox(QuestConfiguration obdaConfiguration, Properties properties)
            throws SemanticIndexException {
        throw new RuntimeException("TODO: implement loadVirtualAbox");
    }

}