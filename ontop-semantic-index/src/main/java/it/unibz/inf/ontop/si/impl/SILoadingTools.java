package it.unibz.inf.ontop.si.impl;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.MappingFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDAModelImpl;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.owlrefplatform.core.abox.RDBMSSIRepositoryManager;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;
import it.unibz.inf.ontop.si.SemanticIndexException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

class SILoadingTools {

    private static final Logger LOG = LoggerFactory.getLogger(OntopSemanticIndexLoaderImpl.class);
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";
    private static final boolean OPTIMIZE_EQUIVALENCES = true;;

    static class RepositoryInit {
        final RDBMSSIRepositoryManager dataRepository;
        final Optional<Set<OWLOntology>> ontologyClosure;
        final String jdbcUrl;
        final ImmutableOntologyVocabulary vocabulary;
        final Connection localConnection;

        private RepositoryInit(RDBMSSIRepositoryManager dataRepository, Optional<Set<OWLOntology>> ontologyClosure, String jdbcUrl,
                               ImmutableOntologyVocabulary vocabulary, Connection localConnection) {
            this.dataRepository = dataRepository;
            this.ontologyClosure = ontologyClosure;
            this.jdbcUrl = jdbcUrl;
            this.vocabulary = vocabulary;
            this.localConnection = localConnection;
        }
    }

    static RepositoryInit createRepository(OWLOntology owlOntology) throws SemanticIndexException {

        Set<OWLOntology> ontologyClosure = owlOntology.getOWLOntologyManager().getImportsClosure(owlOntology);
        Ontology ontology = OWLAPITranslatorUtility.mergeTranslateOntologies(ontologyClosure);
        return createRepository(ontology, Optional.of(ontologyClosure));
    }

    static RepositoryInit createRepository(Ontology ontology) throws SemanticIndexException {
        return createRepository(ontology, Optional.empty());
    }

    private static RepositoryInit createRepository(Ontology ontology, Optional<Set<OWLOntology>> ontologyClosure)
            throws SemanticIndexException {
        ImmutableOntologyVocabulary vocabulary = ontology.getVocabulary();

        final TBoxReasoner reformulationReasoner = TBoxReasonerImpl.create(ontology, OPTIMIZE_EQUIVALENCES);

        RDBMSSIRepositoryManager dataRepository = new RDBMSSIRepositoryManager(reformulationReasoner, vocabulary);

        LOG.warn("Semantic index mode initializing: \nString operation over URI are not supported in this mode ");
        // we work in memory (with H2), the database is clean and
        // Quest will insert new Abox assertions into the database.
        dataRepository.generateMetadata();

        String jdbcUrl = buildNewJdbcUrl();

        try {
            Connection localConnection = DriverManager.getConnection(jdbcUrl, DEFAULT_USER, DEFAULT_PASSWORD);

            // Creating the ABox repository
            dataRepository.createDBSchemaAndInsertMetadata(localConnection);
            return new RepositoryInit(dataRepository, ontologyClosure, jdbcUrl, vocabulary, localConnection);

        } catch (SQLException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }

    static OntopSQLOWLAPIConfiguration createConfiguration(RDBMSSIRepositoryManager dataRepository,
                                                           OWLOntology ontology,
                                                           String jdbcUrl, Properties properties) throws SemanticIndexException {
        return createConfiguration(dataRepository, Optional.of(ontology), jdbcUrl, properties);
    }

    static OntopSQLOWLAPIConfiguration createConfiguration(RDBMSSIRepositoryManager dataRepository,
                                                  String jdbcUrl, Properties properties) throws SemanticIndexException {
        return createConfiguration(dataRepository, Optional.empty(), jdbcUrl, properties);
    }

    private static OntopSQLOWLAPIConfiguration createConfiguration(RDBMSSIRepositoryManager dataRepository,
                                                  Optional<OWLOntology> optionalOntology,
                                                  String jdbcUrl, Properties properties) throws SemanticIndexException {
        OBDAModel ppMapping = createPPMapping(dataRepository);

        /**
         * Tbox: ontology without the ABox axioms (are in the DB now).
         */
        OWLOntologyManager newManager = OWLManager.createOWLOntologyManager();
        Optional<OWLOntology> optionalTBox;
        if (optionalOntology.isPresent()) {
            try {
                OWLOntology tbox = newManager.copyOntology(optionalOntology.get(), OntologyCopy.SHALLOW);
                newManager.removeAxioms(tbox, tbox.getABoxAxioms(Imports.EXCLUDED));
                optionalTBox = Optional.of(tbox);
            } catch (OWLOntologyCreationException e) {
                throw new SemanticIndexException(e.getMessage());
            }
        }
        else
            optionalTBox = Optional.empty();

        OntopSQLOWLAPIConfiguration.Builder builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .obdaModel(ppMapping)
                .properties(properties)
                .jdbcUrl(jdbcUrl)
                .jdbcUser(DEFAULT_USER)
                .jdbcPassword(DEFAULT_PASSWORD)
                //TODO: remove it (required by Tomcat...)
                .jdbcDriver("org.h2.Driver")
                .iriDictionary(dataRepository.getUriMap());

        optionalTBox.ifPresent(builder::ontology);
        return builder.build();
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
