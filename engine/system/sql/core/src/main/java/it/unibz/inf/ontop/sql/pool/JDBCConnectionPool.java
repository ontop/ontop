package it.unibz.inf.ontop.sql.pool;


import java.sql.Connection;
import java.sql.SQLException;

public interface JDBCConnectionPool extends AutoCloseable {

    @Override
    void close();

    Connection getConnection() throws SQLException;
}
