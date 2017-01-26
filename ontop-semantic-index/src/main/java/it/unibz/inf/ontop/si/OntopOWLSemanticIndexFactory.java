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
import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;

/**
 * TODO: find a better name
 */
public class OntopOWLSemanticIndexFactory {

    private static final Logger log = LoggerFactory.getLogger(OntopOWLSemanticIndexFactory.class);

    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";
    private static final boolean OPTIMIZE_EQUIVALENCES = true;

    public static QuestOWL createWithOntologyIndividuals(String owlFile)
            throws OWLOntologyCreationException, SQLException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlFile));
        return createWithOntologyIndividuals(ontology);
    }

    /**
     * TODO: find a better name
     */
    public static QuestOWL createWithOntologyIndividuals(OWLOntology owlOntology)
            throws SQLException, OWLOntologyCreationException {

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
        Connection localConnection = DriverManager.getConnection(jdbcUrl, DEFAULT_USER, DEFAULT_PASSWORD);

        // Creating the ABox repository
        dataRepository.createDBSchemaAndInsertMetadata(localConnection);

        OWLAPIABoxIterator aBoxIter = new OWLAPIABoxIterator(ontologyClosure, vocabulary);

        int count = dataRepository.insertData(localConnection, aBoxIter, 5000, 500);
        log.debug("Inserted {} triples from the ontology.", count);

        QuestOWL reasoner = createReasoner(dataRepository, owlOntology, jdbcUrl);
        localConnection.close();
        return reasoner;
    }

    private static QuestOWL createReasoner(RDBMSSIRepositoryManager dataRepository, OWLOntology owlOntology,
                                           String jdbcUrl) throws OWLOntologyCreationException {
        OBDAModel ppMapping = createPPMapping(dataRepository);

        // TODO: set up the uri map
        QuestConfiguration configuration = QuestConfiguration.defaultBuilder()
                .obdaModel(ppMapping)
                // TODO: could we avoid parsing it again?
                .ontology(owlOntology)
                .jdbcUrl(jdbcUrl)
                .jdbcUser(DEFAULT_USER)
                .jdbcPassword(DEFAULT_PASSWORD)
                .build();

        QuestOWLFactory factory = new QuestOWLFactory();
        return factory.createReasoner(configuration);


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

}
