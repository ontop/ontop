package it.unibz.inf.ontop.si.impl;

import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import it.unibz.inf.ontop.si.repository.impl.SIRepository;
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

        Properties newProperties = new Properties();
        newProperties.putAll(properties);
        // The SI unfortunately does not provide unique constraints... and is not robust to DISTINCTs in a sub-query
        newProperties.putIfAbsent(OntopModelSettings.CARDINALITY_MODE, "LOOSE");

        OntopSQLOWLAPIConfiguration.Builder builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ppMapping(repo.createMappings())
                .properties(newProperties)
                .jdbcUrl(repo.getJdbcUrl())
                .jdbcUser(repo.getUser())
                .jdbcPassword(repo.getPassword())
                .jdbcDriver(repo.getJdbcDriver())
                .keepPermanentDBConnection(true);

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
}
