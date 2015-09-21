package org.semanticweb.ontop.owlrefplatform.core;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants;
import org.semanticweb.ontop.nativeql.DBMetadataException;
import org.semanticweb.ontop.nativeql.DBMetadataExtractor;
import org.semanticweb.ontop.nativeql.JDBCConnectionWrapper;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;
import org.semanticweb.ontop.utils.MetaMappingExpander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.URI;
import java.sql.*;

/**
 * For RDBMS having a JDBC driver.
 */
public class JDBCConnector implements DBConnector {

    private final IQuest questInstance;
    private final QuestPreferences questPreferences;

    /* The active connection used to get metadata from the DBMS */
    private transient Connection localConnection;
    private final OBDADataSource obdaSource;

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
    private final boolean keepAlive;

    private final NativeQueryLanguageComponentFactory nativeQLFactory;

    /**
     * TODO: see if we can ignore the questInstance
     */
    @Inject
    private JDBCConnector(@Assisted OBDADataSource obdaDataSource, @Assisted IQuest questInstance,
                          NativeQueryLanguageComponentFactory nativeQLFactory,
                          QuestPreferences preferences) {
        this.questPreferences = preferences;
        this.obdaSource = obdaDataSource;
        this.questInstance = questInstance;
        this.nativeQLFactory = nativeQLFactory;
        keepAlive = Boolean.valueOf((String) preferences.get(QuestPreferences.KEEP_ALIVE));
        removeAbandoned = Boolean.valueOf((String) preferences.get(QuestPreferences.REMOVE_ABANDONED));
        abandonedTimeout = Integer.valueOf((String) preferences.get(QuestPreferences.ABANDONED_TIMEOUT));
        startPoolSize = Integer.valueOf((String) preferences.get(QuestPreferences.INIT_POOL_SIZE));
        maxPoolSize = Integer.valueOf((String) preferences.get(QuestPreferences.MAX_POOL_SIZE));

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
    public boolean connect() throws OBDAException {
        try {
            if (localConnection != null && !localConnection.isClosed()) {
                return true;
            }
            String url = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
            String username = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
            String password = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
            String driver = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e1) {
                // Does nothing because the SQLException handles this problem also.
            }
            localConnection = DriverManager.getConnection(url, username, password);

            if (localConnection != null) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new OBDAException(e);
        }
    }

    @Override
    public void disconnect() throws OBDAException {
        try {
            localConnection.close();
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
    }

    @Override
    public void dispose() {
/*		try {
			if (evaluationEngine != null)
				this.evaluationEngine.dispose();
		} catch (Exception e) {
			log.debug("Error during disconnect: " + e.getMessage());
		}
*/
        try {
            if (localConnection != null && !localConnection.isClosed())
                disconnect();
        } catch (Exception e) {
            log.debug("Error during disconnect: " + e.getMessage());
        }
    }

    @Override
    public DBMetadata extractDBMetadata(OBDAModel obdaModel, @Nullable ImplicitDBConstraints userConstraints) throws DBMetadataException {
        DBMetadataExtractor dbMetadataExtractor = nativeQLFactory.create();
        return dbMetadataExtractor.extract(obdaSource, obdaModel, new JDBCConnectionWrapper(localConnection), userConstraints);
    }

    @Override
    public OBDAModel expandMetaMappings(OBDAModel unfoldingOBDAModel, URI sourceId) throws OBDAException {
        MetaMappingExpander metaMappingExpander = new MetaMappingExpander(localConnection);
        return metaMappingExpander.expand(unfoldingOBDAModel, sourceId);
    }

    private void setupConnectionPool() {
        String url = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
        String username = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
        String password = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
        String driver = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

        poolProperties = new PoolProperties();
        poolProperties.setUrl(url);
        poolProperties.setDriverClassName(driver);
        poolProperties.setUsername(username);
        poolProperties.setPassword(password);
        poolProperties.setJmxEnabled(true);

        // TEST connection before using it
        poolProperties.setTestOnBorrow(keepAlive);
        if (keepAlive) {
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
        tomcatPool.close();
    }

    public synchronized Connection getSQLPoolConnection() throws OBDAException {
        Connection conn = null;
        try {
            conn = tomcatPool.getConnection();
        } catch (SQLException e) {
            throw new OBDAException(e);
        }
        return conn;
    }

    /***
     * Establishes a new connection to the data source. This is a normal JDBC
     * connection. Used only internally to get metadata at the moment.
     *
     * TODO: update comment
     *
     * @return
     * @throws OBDAException
     */
    protected Connection getSQLConnection() throws OBDAException {
        Connection conn;

        String url = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
        String username = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
        String password = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
        String driver = obdaSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

        // if (driver.contains("mysql")) {
        // url = url + "?relaxAutoCommit=true";
        // }
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e1) {
            log.debug(e1.getMessage());
        }
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new OBDAException(e);
        } catch (Exception e) {
            throw new OBDAException(e);
        }
        return conn;
    }

    // get a real (non pool) connection - used for protege plugin
    @Override
    public IQuestConnection getNonPoolConnection() throws OBDAException {

        return new QuestConnection(this, questInstance, getSQLConnection(), questPreferences);
    }

    /***
     * Returns a QuestConnection, the main object that a client should use to
     * access the query answering services of Quest. With the QuestConnection
     * you can get a QuestStatement to execute queries.
     *
     * <p>
     * Note, the QuestConnection is not a normal JDBC connection. It is a
     * wrapper of one of the N JDBC connections that quest's connection pool
     * starts on initialization. Calling .close() will not actually close the
     * connection, with will just release it back to the pool.
     * <p>
     * to close all connections you must call Quest.close().
     *
     * @return
     * @throws OBDAException
     */
    @Override
    public IQuestConnection getConnection() throws OBDAException {

        return new QuestConnection(this, questInstance, getSQLPoolConnection(), questPreferences);
    }
}
