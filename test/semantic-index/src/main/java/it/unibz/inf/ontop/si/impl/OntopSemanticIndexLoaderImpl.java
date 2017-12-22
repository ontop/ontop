package it.unibz.inf.ontop.si.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import it.unibz.inf.ontop.si.SemanticIndexException;
import it.unibz.inf.ontop.si.repository.SIRepositoryManager;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

/**
 * TODO: find a better name
 */
public class OntopSemanticIndexLoaderImpl implements OntopSemanticIndexLoader {

    private static final Logger LOG = LoggerFactory.getLogger(OntopSemanticIndexLoaderImpl.class);

    private final OntopSQLOWLAPIConfiguration configuration;
    private final Connection connection;

    OntopSemanticIndexLoaderImpl(SIRepository repo, Connection connection, Properties properties, Optional<OWLOntology> tbox) {
        this.connection = connection;

        SQLPPMapping ppMapping = createPPMapping(repo.getDataRepository());

        OntopSQLOWLAPIConfiguration.Builder builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ppMapping(ppMapping)
                .properties(properties)
                .jdbcUrl(repo.getJdbcUrl())
                .jdbcUser(repo.getUser())
                .jdbcPassword(repo.getPassword())
                //TODO: remove it (required by Tomcat...)
                .jdbcDriver("org.h2.Driver")
                .keepPermanentDBConnection(true)
                .iriDictionary(repo.getDataRepository().getUriMap());

        tbox.ifPresent(builder::ontology);

        this.configuration = builder.build();
    }


    @Override
    public OntopSQLOWLAPIConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LOG.error("Error while closing the DB: " + e.getMessage());
        }
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

}
