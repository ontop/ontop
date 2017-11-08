package it.unibz.inf.ontop.answering.connection.pool.impl;


import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;
import it.unibz.inf.ontop.answering.connection.pool.JDBCConnectionPool;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Uses DriverManager
 *
 * Creates as many connection as required.
 *
 * DOES NOT KEEP TRACK OF THEM --> responsibility of the caller to
 * close them!
 *
 */
public class ConnectionGenerator implements JDBCConnectionPool {

    private final OntopSystemSQLSettings settings;

    @Inject
    private ConnectionGenerator(OntopSystemSQLSettings settings) {
        this.settings = settings;
    }


    @Override
    public void close() {
    }

    /**
     * TODO: what about the JDBC driver class name?
     */
    @Override
    public Connection getConnection() throws SQLException {
        return LocalJDBCConnectionUtils.createConnection(settings);
    }
}
