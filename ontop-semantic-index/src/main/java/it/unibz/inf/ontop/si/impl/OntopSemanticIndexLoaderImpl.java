package it.unibz.inf.ontop.si.impl;

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * TODO: find a better name
 */
public class OntopSemanticIndexLoaderImpl implements OntopSemanticIndexLoader {

    private static final Logger LOG = LoggerFactory.getLogger(OntopSemanticIndexLoaderImpl.class);

    private final QuestConfiguration configuration;
    private final Connection connection;


    OntopSemanticIndexLoaderImpl(QuestConfiguration configuration, Connection connection) {
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
            LOG.error("Error while closing the DB: " + e.getMessage());
        }
    }




}
