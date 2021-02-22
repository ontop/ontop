package it.unibz.inf.ontop.answering.connection.pool.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.answering.connection.pool.JDBCConnectionPool;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Does not pool any connection, but creates new ones on demand without keeping track of them.
 *
 * It is therefore essential that caller takes care of closing the connection after using it.
 */
@Singleton
public class DummyJDBCConnectionPool implements JDBCConnectionPool {

    private final OntopSystemSQLSettings settings;

    @Inject
    private DummyJDBCConnectionPool(OntopSystemSQLSettings settings) {
        this.settings = settings;
    }

    @Override
    public void close() {
    }

    @Override
    public Connection getConnection() throws SQLException {
        return LocalJDBCConnectionUtils.createConnection(settings);
    }
}
