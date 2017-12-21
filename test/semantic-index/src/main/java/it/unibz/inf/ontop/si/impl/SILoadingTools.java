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
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import it.unibz.inf.ontop.si.repository.impl.RDBMSSIRepositoryManager;
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

    static class RepositoryInit {
        final SIRepositoryManager dataRepository;
        final String jdbcUrl;
        final Connection localConnection;

        private RepositoryInit(SIRepositoryManager dataRepository, String jdbcUrl,
                               Connection localConnection) {
            this.dataRepository = dataRepository;
            this.jdbcUrl = jdbcUrl;
            this.localConnection = localConnection;
        }
    }

    static RepositoryInit createRepository(ClassifiedTBox tbox) throws SemanticIndexException {

        SIRepositoryManager dataRepository = new RDBMSSIRepositoryManager(tbox);

        LOG.warn("Semantic index mode initializing: \nString operation over URI are not supported in this mode ");

        String jdbcUrl = buildNewJdbcUrl();

        try {
            Connection localConnection = DriverManager.getConnection(jdbcUrl, DEFAULT_USER, DEFAULT_PASSWORD);

            // Creating the ABox repository
            dataRepository.createDBSchemaAndInsertMetadata(localConnection);
            return new RepositoryInit(dataRepository, jdbcUrl, localConnection);
        }
        catch (SQLException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }


    static OntopSQLOWLAPIConfiguration createConfiguration(SIRepositoryManager dataRepository,
                                                           OWLOntology ontology,
                                                           String jdbcUrl, Properties properties) throws SemanticIndexException {
        SQLPPMapping ppMapping = createPPMapping(dataRepository);

        //Tbox: ontology without the ABox axioms (are in the DB now).
        OWLOntology tbox;
        try {
            OWLOntologyManager newManager = OWLManager.createOWLOntologyManager();
            tbox = newManager.copyOntology(ontology, OntologyCopy.SHALLOW);
            newManager.removeAxioms(tbox, tbox.getABoxAxioms(Imports.EXCLUDED));
        }
        catch (OWLOntologyCreationException e) {
            throw new SemanticIndexException(e.getMessage());
        }

        OntopSQLOWLAPIConfiguration.Builder builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ppMapping(ppMapping)
                .properties(properties)
                .jdbcUrl(jdbcUrl)
                .jdbcUser(DEFAULT_USER)
                .jdbcPassword(DEFAULT_PASSWORD)
                //TODO: remove it (required by Tomcat...)
                .jdbcDriver("org.h2.Driver")
                .keepPermanentDBConnection(true)
                .iriDictionary(dataRepository.getUriMap())
                .ontology(tbox);

        return builder.build();
    }

    static OntopSQLOWLAPIConfiguration createConfigurationWithoutTBox(SIRepositoryManager dataRepository,
                                                  String jdbcUrl, Properties properties) {
        SQLPPMapping ppMapping = createPPMapping(dataRepository);

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
        }
        catch (DuplicateMappingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private static String buildNewJdbcUrl() {
        return "jdbc:h2:mem:questrepository:" + UUID.randomUUID() + ";LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0";
    }
}
