package it.unibz.inf.ontop.utils;

import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class LocalJDBCConnectionUtils {

    /**
     * Brings robustness to some Tomcat classloading issues.
     */
    public static Connection createConnection(OntopSQLCredentialSettings settings) throws SQLException {

        Properties jdbcInfo = new Properties();
        settings.getJdbcUser()
                .ifPresent(u -> jdbcInfo.put("user", u));
        settings.getJdbcPassword()
                .ifPresent(p -> jdbcInfo.put("password", p));

        try {
            // This should work in most cases (e.g. from CLI, Protege, or Jetty)
            return DriverManager.getConnection(settings.getJdbcUrl(), jdbcInfo);
        } catch (SQLException ex) {
            // HACKY(xiao): This part is still necessary for Tomcat.
            // Otherwise, JDBC drivers are not initialized by default.
            try {
                Class.forName(settings.getJdbcDriver());
            } catch (ClassNotFoundException e) {
                throw new SQLException("Cannot load the driver: " + e.getMessage());
            }

            return DriverManager.getConnection(settings.getJdbcUrl(), jdbcInfo);
        }
    }

    public static Connection createLazyConnection(OntopSQLCredentialSettings settings) {
        return new AbstractLazyConnection() {

            //Lazy
            private Connection connection;

            @Override
            protected synchronized Connection getConnection() throws SQLException {
                if (connection == null)
                    connection = createConnection(settings);

                return connection;
            }

            @Override
            protected void closeConnection() throws SQLException {
                if (connection != null)
                    connection.close();
            }
        };
    }

    /**
     * Agnostic on how the connection is created
     */
    public static abstract class AbstractLazyConnection implements Connection {
        private boolean isClosed = false;

        protected abstract Connection getConnection() throws SQLException;
        protected abstract void closeConnection() throws SQLException;

        @Override
        public Statement createStatement() throws SQLException {
            return getConnection().createStatement();
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return getConnection().prepareStatement(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            return getConnection().prepareCall(sql);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return getConnection().nativeSQL(sql);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            getConnection().setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return getConnection().getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {
            getConnection().commit();
        }

        @Override
        public void rollback() throws SQLException {
            getConnection().rollback();
        }

        @Override
        public void close() throws SQLException {
            isClosed = true;
            closeConnection();
        }

        @Override
        public boolean isClosed() throws SQLException {
            return isClosed;
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return getConnection().getMetaData();
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            getConnection().setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return getConnection().isReadOnly();
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            getConnection().setCatalog(catalog);
        }

        @Override
        public String getCatalog() throws SQLException {
            return getConnection().getCatalog();
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            getConnection().setTransactionIsolation(level);
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return getConnection().getTransactionIsolation();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return getConnection().getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException {
            getConnection().clearWarnings();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return getConnection().createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return getConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return getConnection().getTypeMap();
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            getConnection().setTypeMap(map);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            getConnection().setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {
            return getConnection().getHoldability();
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return getConnection().setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            return getConnection().setSavepoint(name);
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            getConnection().rollback(savepoint);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            getConnection().releaseSavepoint(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return getConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return getConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return getConnection().prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return getConnection().prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return getConnection().prepareStatement(sql, columnNames);
        }

        @Override
        public Clob createClob() throws SQLException {
            return getConnection().createClob();
        }

        @Override
        public Blob createBlob() throws SQLException {
            return getConnection().createBlob();
        }

        @Override
        public NClob createNClob() throws SQLException {
            return getConnection().createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return getConnection().createSQLXML();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return getConnection().isValid(timeout);
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            try {
                getConnection().setClientInfo(name, value);
            } catch (SQLClientInfoException e) {
                throw e;
            } catch (SQLException e) {
            }
        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            try {
                getConnection().setClientInfo(properties);
            } catch (SQLClientInfoException e) {
                throw e;
            } catch (SQLException e) {
            }
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return getConnection().getClientInfo(name);
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return getConnection().getClientInfo();
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return getConnection().createArrayOf(typeName, elements);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return getConnection().createStruct(typeName, attributes);
        }

        @Override
        public void setSchema(String schema) throws SQLException {
            getConnection().setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException {
            return getConnection().getSchema();
        }

        @Override
        public void abort(Executor executor) throws SQLException {
            getConnection().abort(executor);
        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            getConnection().setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return getConnection().getNetworkTimeout();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return getConnection().unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return getConnection().isWrapperFor(iface);
        }
    }

}
