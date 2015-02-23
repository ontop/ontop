package org.semanticweb.ontop.nativeql;

import java.sql.Connection;

/**
 * JDBC DBConnectionWrapper.
 */
public class JDBCConnectionWrapper implements DBConnectionWrapper {

    private final Connection connection;

    public JDBCConnectionWrapper(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}
