package it.unibz.inf.ontop.si.impl;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import it.unibz.inf.ontop.si.SemanticIndexException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static it.unibz.inf.ontop.si.impl.SILoadingTools.createConfiguration;
import static it.unibz.inf.ontop.si.impl.SILoadingTools.createConfigurationWithoutTBox;

/**
 * TODO: find a better name
 */
public class OntopSemanticIndexLoaderImpl implements OntopSemanticIndexLoader {

    private static final Logger LOG = LoggerFactory.getLogger(OntopSemanticIndexLoaderImpl.class);

    private final OntopSQLOWLAPIConfiguration configuration;
    private final Connection connection;


    OntopSemanticIndexLoaderImpl(SILoadingTools.RepositoryInit init, Properties properties) {
        this.configuration = createConfigurationWithoutTBox(init.dataRepository, init.jdbcUrl, properties);
        this.connection = init.localConnection;
    }

    OntopSemanticIndexLoaderImpl(SILoadingTools.RepositoryInit init, Properties properties, OWLOntology owlOntology) throws SemanticIndexException {
        this.configuration = createConfiguration(init.dataRepository, owlOntology, init.jdbcUrl, properties);
        this.connection = init.localConnection;
    }




    @Override
    public OntopSQLOWLAPIConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void close() {
        try {
            if (connection != null && (!connection.isClosed())) {
                connection.close();
            }
        } catch (SQLException e) {
            LOG.error("Error while closing the DB: " + e.getMessage());
        }
    }
}
