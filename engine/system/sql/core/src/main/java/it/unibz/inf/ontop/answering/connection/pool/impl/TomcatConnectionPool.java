package it.unibz.inf.ontop.answering.connection.pool.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;
import it.unibz.inf.ontop.answering.connection.pool.JDBCConnectionPool;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;


/**
 * Not a SINGLETON!
 */
public class TomcatConnectionPool implements JDBCConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(TomcatConnectionPool.class);
    private final DataSource tomcatPool;

    @Inject
    private TomcatConnectionPool(OntopSystemSQLSettings settings) {
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setUrl(settings.getJdbcUrl());
        poolProperties.setDriverClassName(settings.getJdbcDriver());
        settings.getJdbcUser()
                .ifPresent(poolProperties::setUsername);
        settings.getJdbcPassword()
                .ifPresent(poolProperties::setPassword);
        poolProperties.setJmxEnabled(true);

        // TEST connection before using it
        boolean keepAlive = settings.isKeepAliveEnabled();
        poolProperties.setTestOnBorrow(keepAlive);
        if (keepAlive) {
            // TODO: refactor this
            String driver = settings.getJdbcDriver();
            if (driver.contains("oracle"))
                poolProperties.setValidationQuery("select 1 from dual");
            else if (driver.contains("db2"))
                poolProperties.setValidationQuery("select 1 from sysibm.sysdummy1");
            else
                poolProperties.setValidationQuery("select 1");
        }

        boolean removeAbandoned = settings.isRemoveAbandonedEnabled();
        int abandonedTimeout = settings.getConnectionTimeout();
        int startPoolSize = settings.getConnectionPoolInitialSize();
        int maxPoolSize = settings.getConnectionPoolMaxSize();
        int timeout = settings.getConnectionTimeout();

        poolProperties.setTestOnReturn(false);
        poolProperties.setMaxActive(maxPoolSize);
        poolProperties.setMaxIdle(maxPoolSize);
        poolProperties.setInitialSize(startPoolSize);
        poolProperties.setMaxWait(timeout);
        poolProperties.setRemoveAbandonedTimeout(abandonedTimeout);
        poolProperties.setMinEvictableIdleTimeMillis(timeout);
        poolProperties.setLogAbandoned(false);
        poolProperties.setRemoveAbandoned(removeAbandoned);
        poolProperties.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        tomcatPool = new DataSource();
        tomcatPool.setPoolProperties(poolProperties);

        LOGGER.debug("Connection Pool Properties:");
        LOGGER.debug("Start size: " + startPoolSize);
        LOGGER.debug("Max size: " + maxPoolSize);
        LOGGER.debug("Remove abandoned connections: " + removeAbandoned);
    }

    @Override
    public void close() {
        tomcatPool.close();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return tomcatPool.getConnection();
    }
}
