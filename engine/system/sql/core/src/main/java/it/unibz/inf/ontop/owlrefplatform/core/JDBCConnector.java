package it.unibz.inf.ontop.owlrefplatform.core;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.input.InputQueryFactory;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.reformulation.QueryTranslator;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.Optional;

/**
 * For RDBMS having a JDBC driver.
 */
public class JDBCConnector implements DBConnector {

    private final QueryTranslator queryReformulator;

    private final OntopSystemSQLSettings settings;
    private final Optional<IRIDictionary> iriDictionary;

    /* The active connection used to get metadata from the DBMS */
    private transient Connection localConnection;

    private final Logger log = LoggerFactory.getLogger(JDBCConnector.class);
    private PoolProperties poolProperties;
    private DataSource tomcatPool;

    // Tomcat pool default properties
    // These can be changed in the properties file
    private final int maxPoolSize;
    private final int startPoolSize;
    private final boolean removeAbandoned ;
    private final boolean logAbandoned = false;
    private final int abandonedTimeout;
    private final DBMetadata dbMetadata;
    private final InputQueryFactory inputQueryFactory;
    private final boolean keepAlive;

    @AssistedInject
    private JDBCConnector(@Assisted QueryTranslator queryTranslator,
                          @Assisted DBMetadata dbMetadata,
                          @Nullable IRIDictionary iriDictionary,
                          InputQueryFactory inputQueryFactory,
                          OntopSystemSQLSettings settings) {
        this.queryReformulator = queryTranslator;
        this.dbMetadata = dbMetadata;
        this.inputQueryFactory = inputQueryFactory;
        keepAlive = settings.isKeepAliveEnabled();
        removeAbandoned = settings.isRemoveAbandonedEnabled();
        abandonedTimeout = settings.getAbandonedTimeout();
        startPoolSize = settings.getConnectionPoolInitialSize();
        maxPoolSize = settings.getConnectionPoolMaxSize();
        this.settings = settings;
        this.iriDictionary = Optional.ofNullable(iriDictionary);

        setupConnectionPool();
    }

    /***
     * Starts the local connection that Quest maintains to the DBMS. This
     * connection belongs only to Quest and is used to get information from the
     * DBMS. At the moment this connection is mainly used during initialization,
     * to get metadata about the DBMS or to create repositories in classic mode.
     *
     * @return
     * @throws SQLException
     */
    public boolean connect() throws OntopConnectionException {
        try {
            if (localConnection != null && !localConnection.isClosed()) {
                return true;
            }

//            try {
//                Class.forName(settings.getJdbcDriver());
//            } catch (ClassNotFoundException e1) {
//                // Does nothing because the SQLException handles this problem also.
//            }
            localConnection = DriverManager.getConnection(settings.getJdbcUrl(),
                    settings.getJdbcUser(), settings.getJdbcPassword());

            if (localConnection != null) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }
    }

    private void setupConnectionPool() {
        poolProperties = new PoolProperties();
        poolProperties.setUrl(settings.getJdbcUrl());
        settings.getJdbcDriver()
                .ifPresent(d -> poolProperties.setDriverClassName(d));
        poolProperties.setUsername(settings.getJdbcUser());
        poolProperties.setPassword(settings.getJdbcPassword());
        poolProperties.setJmxEnabled(true);

        // TEST connection before using it
        poolProperties.setTestOnBorrow(keepAlive);
        if (keepAlive) {
            // TODO: refactor this
            String driver = settings.getJdbcDriver()
                    .orElse("");
            if (driver.contains("oracle"))
                poolProperties.setValidationQuery("select 1 from dual");
            else if (driver.contains("db2"))
                poolProperties.setValidationQuery("select 1 from sysibm.sysdummy1");
            else
                poolProperties.setValidationQuery("select 1");
        }

        poolProperties.setTestOnReturn(false);
        poolProperties.setMaxActive(maxPoolSize);
        poolProperties.setMaxIdle(maxPoolSize);
        poolProperties.setInitialSize(startPoolSize);
        poolProperties.setMaxWait(30000);
        poolProperties.setRemoveAbandonedTimeout(abandonedTimeout);
        poolProperties.setMinEvictableIdleTimeMillis(30000);
        poolProperties.setLogAbandoned(logAbandoned);
        poolProperties.setRemoveAbandoned(removeAbandoned);
        poolProperties.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        tomcatPool = new DataSource();
        tomcatPool.setPoolProperties(poolProperties);

        log.debug("Connection Pool Properties:");
        log.debug("Start size: " + startPoolSize);
        log.debug("Max size: " + maxPoolSize);
        log.debug("Remove abandoned connections: " + removeAbandoned);
    }

    @Override
    public void close() {
        try {
            if (localConnection != null)
                localConnection.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        tomcatPool.close();
    }

    public synchronized Connection getSQLPoolConnection() throws OntopConnectionException {
        try {
            return tomcatPool.getConnection();
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

        return new QuestConnection(this, queryReformulator, getSQLPoolConnection(), iriDictionary,
                dbMetadata, inputQueryFactory, settings);
    }


}
