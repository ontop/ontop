package it.unibz.inf.ontop.si.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.si.repository.SIRepositoryManager;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.spec.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.si.repository.impl.RDBMSSIRepositoryManager;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.spec.ontology.impl.TBoxReasonerImpl;
import it.unibz.inf.ontop.si.SemanticIndexException;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
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
import java.util.UUID;

class SILoadingTools {

    private static final Logger LOG = LoggerFactory.getLogger(OntopSemanticIndexLoaderImpl.class);
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";
    private static final boolean OPTIMIZE_EQUIVALENCES = true;;

    static class RepositoryInit {
        final SIRepositoryManager dataRepository;
        final Optional<Set<OWLOntology>> ontologyClosure;
        final String jdbcUrl;
        final ImmutableOntologyVocabulary vocabulary;
        final Connection localConnection;

        private RepositoryInit(SIRepositoryManager dataRepository, Optional<Set<OWLOntology>> ontologyClosure, String jdbcUrl,
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

        TBoxReasoner reformulationReasoner = TBoxReasonerImpl.create(ontology);

        SIRepositoryManager dataRepository = new RDBMSSIRepositoryManager(reformulationReasoner);

        LOG.warn("Semantic index mode initializing: \nString operation over URI are not supported in this mode ");
        // we work in memory (with H2), the database is clean and
        // Quest will insert new Abox assertions into the database.
        dataRepository.generateMetadata();

        String jdbcUrl = buildNewJdbcUrl();

        try {
            Connection localConnection = DriverManager.getConnection(jdbcUrl, DEFAULT_USER, DEFAULT_PASSWORD);

            // Creating the ABox repository
            dataRepository.createDBSchemaAndInsertMetadata(localConnection);
            return new RepositoryInit(dataRepository, ontologyClosure, jdbcUrl, reformulationReasoner.getVocabulary(), localConnection);

        } catch (SQLException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }

    static OntopSQLOWLAPIConfiguration createConfiguration(SIRepositoryManager dataRepository,
                                                           OWLOntology ontology,
                                                           String jdbcUrl, Properties properties) throws SemanticIndexException {
        return createConfiguration(dataRepository, Optional.of(ontology), jdbcUrl, properties);
    }

    static OntopSQLOWLAPIConfiguration createConfiguration(SIRepositoryManager dataRepository,
                                                  String jdbcUrl, Properties properties) throws SemanticIndexException {
        return createConfiguration(dataRepository, Optional.empty(), jdbcUrl, properties);
    }

    private static OntopSQLOWLAPIConfiguration createConfiguration(SIRepositoryManager dataRepository,
                                                  Optional<OWLOntology> optionalOntology,
                                                  String jdbcUrl, Properties properties) throws SemanticIndexException {
        SQLPPMapping ppMapping = createPPMapping(dataRepository);

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
                .ppMapping(ppMapping)
                .properties(properties)
                .jdbcUrl(jdbcUrl)
                .jdbcUser(DEFAULT_USER)
                .jdbcPassword(DEFAULT_PASSWORD)
                //TODO: remove it (required by Tomcat...)
                .jdbcDriver("org.h2.Driver")
                .keepPermanentDBConnection(true)
                .iriDictionary(dataRepository.getUriMap());

        optionalTBox.ifPresent(builder::ontology);
        return builder.build();
    }

    private static SQLPPMapping createPPMapping(SIRepositoryManager dataRepository) {
        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .build();
        SpecificationFactory specificationFactory = defaultConfiguration.getInjector().getInstance(SpecificationFactory.class);
        PrefixManager prefixManager = specificationFactory.createPrefixManager(ImmutableMap.of());

        ImmutableList<SQLPPTriplesMap> mappingAxioms = dataRepository.getMappings();

        UriTemplateMatcher uriTemplateMatcher = UriTemplateMatcher.create(
                mappingAxioms.stream()
                        .flatMap(ax -> ax.getTargetAtoms().stream())
                        .flatMap(atom -> atom.getArguments().stream())
                        .filter(t -> t instanceof ImmutableFunctionalTerm)
                        .map(t -> (ImmutableFunctionalTerm) t));

        try {
            return new SQLPPMappingImpl(mappingAxioms,
                    specificationFactory.createMetadata(prefixManager, uriTemplateMatcher));

        } catch (DuplicateMappingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private static String buildNewJdbcUrl() {
        return "jdbc:h2:mem:questrepository:" + UUID.randomUUID() + ";LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0";
    }
}
