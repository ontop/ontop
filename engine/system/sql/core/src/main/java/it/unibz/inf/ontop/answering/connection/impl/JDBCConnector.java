package it.unibz.inf.ontop.answering.connection.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.connection.DBConnector;
import it.unibz.inf.ontop.answering.connection.JDBCStatementInitializer;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.connection.pool.JDBCConnectionPool;

import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;
import org.apache.commons.rdf.api.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * For RDBMS having a JDBC driver.
 */
public class JDBCConnector implements DBConnector {

    private final QueryReformulator queryReformulator;

    private final SubstitutionFactory substitutionFactory;
    private final OntopSystemSQLSettings settings;

    /* The active connection used for keeping in-memory DBs alive */
    private transient Connection localConnection;

    private final Logger log = LoggerFactory.getLogger(JDBCConnector.class);
    private final JDBCConnectionPool connectionPool;

    private final TermFactory termFactory;
    private final RDF rdfFactory;
    private final JDBCStatementInitializer statementInitializer;

    @AssistedInject
    private JDBCConnector(@Assisted QueryReformulator queryReformulator,
                          JDBCConnectionPool connectionPool,
                          TermFactory termFactory,
                          SubstitutionFactory substitutionFactory,
                          RDF rdfFactory,
                          JDBCStatementInitializer statementInitializer,
                          OntopSystemSQLSettings settings) {
        this.queryReformulator = queryReformulator;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.settings = settings;
        this.connectionPool = connectionPool;
        this.rdfFactory = rdfFactory;
        this.statementInitializer = statementInitializer;
    }

    /**
     * Keeps a permanent connection to the DB (if enabled in the settings).
     *
     * Needed by some in-memory DBs (such as H2).
     *
     */
    public boolean connect() throws OntopConnectionException {
        try {
            if (localConnection != null && !localConnection.isClosed()) {
                return true;
            }
            if (settings.isPermanentDBConnectionEnabled()) {
                localConnection = LocalJDBCConnectionUtils.createConnection(settings);
                return localConnection != null;
            }
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }

        return true;
    }

    @Override
    public void close() {
        try {
            if (localConnection != null)
                localConnection.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        connectionPool.close();
    }

    public synchronized Connection getSQLPoolConnection() throws OntopConnectionException {
        try {
            return connectionPool.getConnection();
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }
    }

    /***
     * Returns an OntopConnection, the main object that a client should use to
     * access the query answering services of Quest. With the QuestConnection
     * you can get a QuestStatement to execute queries.
     *
     * <p>
     * Note, the OntopConnection is not a normal JDBC connection. It is a
     * wrapper of one of the N JDBC connections that quest's connection pool
     * starts on initialization. Calling .close() will not actually close the
     * connection, with will just release it back to the pool.
     * <p>
     * to close all connections you must call DBConnector.close().
     *
     */
    @Override
    public OntopConnection getConnection() throws OntopConnectionException {

        return new SQLConnection(this, queryReformulator, getSQLPoolConnection(),
                termFactory, rdfFactory, substitutionFactory, statementInitializer, settings);
    }


}
