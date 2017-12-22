package it.unibz.inf.ontop.si.impl;


import it.unibz.inf.ontop.si.repository.SIRepositoryManager;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.si.repository.impl.RDBMSSIRepositoryManager;
import it.unibz.inf.ontop.si.SemanticIndexException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

public class SIRepository {

    private static final Logger LOG = LoggerFactory.getLogger(SIRepository.class);
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";

    private final SIRepositoryManager dataRepository;
    private final String jdbcUrl;
    private final String user;
    private final String password;

    public SIRepository(ClassifiedTBox tbox) {

        this.dataRepository = new RDBMSSIRepositoryManager(tbox);

        LOG.warn("Semantic index mode initializing: \nString operation over URI are not supported in this mode ");

        this.jdbcUrl = "jdbc:h2:mem:questrepository:" + UUID.randomUUID() + ";LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0";

        this.user = DEFAULT_USER;
        this.password = DEFAULT_PASSWORD;
    }

    public SIRepositoryManager getDataRepository() { return dataRepository; }

    public String getJdbcUrl() { return jdbcUrl; }

    public String getUser() { return user; }

    public String getPassword(){ return password; }

    public Connection createConnection() throws SemanticIndexException {

        try {
            Connection localConnection = DriverManager.getConnection(jdbcUrl, user, password);

            // Creating the ABox repository
            dataRepository.createDBSchemaAndInsertMetadata(localConnection);
            return localConnection;
        }
        catch (SQLException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }
}
