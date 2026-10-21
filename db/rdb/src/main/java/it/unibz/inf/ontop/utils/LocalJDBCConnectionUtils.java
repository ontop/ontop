package it.unibz.inf.ontop.utils;

import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class LocalJDBCConnectionUtils {

    private static final Logger logger = LoggerFactory.getLogger(LocalJDBCConnectionUtils.class);

    /**
     * Brings robustness to some Tomcat classloading issues.
     */
    public static Connection createConnection(OntopSQLCredentialSettings settings) throws SQLException {

        Properties jdbcInfo = new Properties();
        jdbcInfo.putAll(settings.getAdditionalJDBCProperties());
        settings.getJdbcUser()
                .ifPresent(u -> jdbcInfo.put("user", u));
        settings.getJdbcPassword()
                .ifPresent(p -> jdbcInfo.put("password", p));
        
        // Check if we need to add Authorization header as accessToken property
        String authToken = getAuthorizationTokenFromContext();
        if (authToken != null) {
            jdbcInfo.put("accessToken", authToken);
            logger.info("Added Authorization token to JDBC properties");
        }

        // Check if we need to modify JDBC URL to remove accessToken
        String originalUrl = settings.getJdbcUrl();
        String jdbcUrl = processJdbcUrlForAuthorization(originalUrl);
        
        // Log JDBC URL processing
        logger.debug("Creating connection with URL: {}", originalUrl);
        if (!originalUrl.equals(jdbcUrl)) {
            logger.info("URL was modified to: {}", jdbcUrl);
        } else {
            logger.debug("URL was not modified");
        }

        Connection connection;
        try {
            // This should work in most cases (e.g. from CLI, Protege, or Jetty)
            connection = DriverManager.getConnection(jdbcUrl, jdbcInfo);
        } catch (SQLException ex) {
            // HACKY(xiao): This part is still necessary for Tomcat.
            // Otherwise, JDBC drivers are not initialized by default.
            try {
                Class.forName(settings.getJdbcDriver());
            } catch (ClassNotFoundException e) {
                throw new SQLException("Cannot load the driver: " + e.getMessage());
            }

            connection = DriverManager.getConnection(jdbcUrl, jdbcInfo);
        }
        if (!settings.initScript().isEmpty()) {
            runInitializationScript(connection, settings.initScript());
        }
        return connection;
    }

    private static void runInitializationScript(Connection connection, String dbInitScript) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(dbInitScript);
        } catch (SQLException e) {
            throw new SQLException("Cannot execute the initialization script: " + e.getMessage());
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
        public boolean isClosed()  {
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
            } catch (SQLException ignored) {
            }
        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            try {
                getConnection().setClientInfo(properties);
            } catch (SQLClientInfoException e) {
                throw e;
            } catch (SQLException ignored) {
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

    /**
     * Processes JDBC URL to remove accessToken parameter if Authorization header is present.
     */
    private static String processJdbcUrlForAuthorization(String originalUrl) {
        // Check if we have an Authorization header via thread-local context
        boolean hasAuthHeader = hasAuthorizationHeaderInContext();
        
        if (!hasAuthHeader) {
            return originalUrl;
        }
        
        // Remove accessToken parameter from JDBC URL
        String modifiedUrl = removeAccessTokenFromUrl(originalUrl);
        
        // Log the URL modification for debugging
        if (!originalUrl.equals(modifiedUrl)) {
            logger.info("JDBC URL modified: accessToken removed due to Authorization header");
            logger.debug("Original: {}", originalUrl);
            logger.debug("Modified: {}", modifiedUrl);
        }
        
        return modifiedUrl;
    }
    
    /**
     * Checks if Authorization header is present via thread-local context using reflection.
     */
    private static boolean hasAuthorizationHeaderInContext() {
        try {
            // Use reflection to access AuthorizationContext to avoid circular dependencies
            Class<?> authContextClass = Class.forName("it.unibz.inf.ontop.endpoint.processor.AuthorizationContext");
            java.lang.reflect.Method hasAuthMethod = authContextClass.getMethod("hasAuthorizationHeader");
            return (Boolean) hasAuthMethod.invoke(null);
        } catch (Exception e) {
            // If AuthorizationContext is not available or any error occurs, assume no auth header
            return false;
        }
    }
    
    /**
     * Gets the Authorization token from thread-local context using reflection.
     */
    private static String getAuthorizationTokenFromContext() {
        try {
            // Use reflection to access AuthorizationContext to avoid circular dependencies
            Class<?> authContextClass = Class.forName("it.unibz.inf.ontop.endpoint.processor.AuthorizationContext");
            java.lang.reflect.Method getTokenMethod = authContextClass.getMethod("getAuthorizationToken");
            return (String) getTokenMethod.invoke(null);
        } catch (Exception e) {
            // If AuthorizationContext is not available or any error occurs, return null
            return null;
        }
    }
    
    /**
     * Removes accessToken parameter from JDBC URL.
     */
    private static String removeAccessTokenFromUrl(String jdbcUrl) {
        if (jdbcUrl == null) {
            return null;
        }
        
        logger.debug("Before accessToken removal: {}", jdbcUrl);
        
        // More careful approach to remove accessToken parameter
        String result = jdbcUrl;
        
        // Case 1: ?accessToken=value& (accessToken is first parameter, followed by others)
        result = result.replaceAll("\\?accessToken=[^&]*&", "?");
        
        // Case 2: ?accessToken=value (accessToken is the only parameter)
        result = result.replaceAll("\\?accessToken=[^&]*$", "");
        
        // Case 3: &accessToken=value (accessToken is not the first parameter)
        result = result.replaceAll("&accessToken=[^&]*", "");
        
        logger.debug("After accessToken removal: {}", result);
        
        return result;
    }

}
