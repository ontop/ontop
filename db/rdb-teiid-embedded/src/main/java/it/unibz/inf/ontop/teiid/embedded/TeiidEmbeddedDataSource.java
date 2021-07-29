package it.unibz.inf.ontop.teiid.embedded;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teiid.adminapi.Model;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.adminapi.impl.SourceMappingMetadata;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.cache.caffeine.CaffeineCacheFactory;
import org.teiid.deployers.VDBRepository;
import org.teiid.deployers.VirtualDatabaseException;
import org.teiid.dqp.internal.datamgr.ConnectorManagerRepository.ConnectorManagerException;
import org.teiid.metadatastore.DeploymentBasedDatabaseStore;
import org.teiid.runtime.EmbeddedConfiguration;
import org.teiid.runtime.EmbeddedServer;
import org.teiid.translator.TranslatorException;
import org.teiid.transport.SocketConfiguration;
import org.teiid.transport.WireProtocol;

public class TeiidEmbeddedDataSource implements DataSource, Closeable {

    // TODO: closing a data source should remove it from the map
    // TODO: use soft references and intercept GC

    private static final Logger LOGGER = LoggerFactory.getLogger(TeiidEmbeddedDataSource.class);

    private static final Map<HashCode, TeiidEmbeddedDataSource> DATA_SOURCES = Maps.newHashMap();

    private final Map<String, Object> connectionFactories;

    private final EmbeddedServer server;

    private final String url;

    static {
        // Configure TEIID
        System.setProperty("org.teiid.comparableLobs", "true");
        System.setProperty("org.teiid.maxStringLength", "65535");

        // Configure transaction manager type (in-memory)
        System.setProperty("ObjectStoreEnvironmentBean.objectStoreType",
                "com.arjuna.ats.internal.arjuna.objectstore.VolatileStore");
        System.setProperty("ObjectStoreEnvironmentBean.stateStore.objectStoreType",
                "com.arjuna.ats.internal.arjuna.objectstore.VolatileStore");
        System.setProperty("ObjectStoreEnvironmentBean.communicationStore.objectStoreType",
                "com.arjuna.ats.internal.arjuna.objectstore.VolatileStore");

        // Configure transaction manager storage location (not needed for in-memory)
        System.setProperty("ObjectStoreEnvironmentBean.objectStoreDir",
                "/tmp/narayana-object-store");
        System.setProperty("com.arjuna.ats.arjuna.objectstore.objectStoreDir",
                "/tmp/narayana-object-store");

        // Register shutdown hook to close data sources
        final Thread shutdownHandler = new Thread("shutdown") {

            @Override
            public void run() {
                for (TeiidEmbeddedDataSource ds : DATA_SOURCES.values()) {
                    try {
                        ds.close();
                    } catch (Throwable ex) {
                        // ignore
                    }
                }
            }

        };
        Runtime.getRuntime().addShutdownHook(shutdownHandler);
    }

    // INITIALIZATION

    private TeiidEmbeddedDataSource(final String ddlContent) throws IOException,
            TranslatorException, ConnectorManagerException, VirtualDatabaseException {

        // Parse DDL into a VDBMetaData object
        DeploymentBasedDatabaseStore store;
        store = new DeploymentBasedDatabaseStore(new VDBRepository());
        final VDBMetaData metadata = store.getVDBMetadata(ddlContent);

        // Collect server and translator names mentioned in the DDL
        final Set<String> serverNames = Sets.newHashSet();
        final Set<String> translatorNames = Sets.newHashSet();
        for (final ModelMetaData model : metadata.getModelMetaDatas().values()) {
            if (model.getModelType() == Model.Type.PHYSICAL) {
                for (final SourceMappingMetadata mapping : model.getSourceMappings()) {
                    serverNames.add(mapping.getName());
                    translatorNames.add(mapping.getTranslatorName());
                }
            }
        }

        // Configure exported JDBC port
        SocketConfiguration sc = null;
        final String export = metadata.getPropertyValue("export");
        if (export != null) {
            final int index = export.indexOf(":");
            sc = new SocketConfiguration();
            sc.setProtocol(WireProtocol.teiid);
            sc.setBindAddress(index >= 0 ? export.substring(0, index).trim() : "0.0.0.0");
            sc.setPortNumber(
                    Integer.valueOf((index >= 0 ? export.substring(index + 1) : export).trim()));
        }

        // Create Teiid embedded server
        final EmbeddedServer server = new EmbeddedServer();

        // Register a DataSource/ConnectionFactory for each server in the DDL
        final Map<String, Object> connectionFactories = Maps.newHashMap();
        for (final String serverName : serverNames) {
            final String serverUrl = metadata.getPropertyValue("server." + serverName);
            final Object connectionFactory = TeiidEmbeddedUtils.getConnectionFactory(serverUrl);
            server.addConnectionFactory(serverName, connectionFactory);
            connectionFactories.put(serverName, connectionFactory);
        }

        // Register an ExecutionFactory for each translator name mentioned in the DDL
        for (final String translatorName : translatorNames) {
            server.addTranslator(translatorName,
                    TeiidEmbeddedUtils.getExecutionFactory(translatorName));
        }

        // Complete server configuration and start the server
        final EmbeddedConfiguration config = new EmbeddedConfiguration();
        config.setCacheFactory(new CaffeineCacheFactory());
        config.setTransactionManager(com.arjuna.ats.jta.TransactionManager.transactionManager()); // TODO
        if (sc != null) {
            config.addTransport(sc);
        }
        server.start(config);

        // Deploy the VDB (note: have to parse the DDL again due to need of populating a
        // VDBRepository internal to the server)
        server.deployVDB(new ByteArrayInputStream(ddlContent.getBytes()), true);

        // Initialize state
        this.connectionFactories = connectionFactories;
        this.server = server;
        this.url = "jdbc:teiid:" + metadata.getName();
    }

    public static TeiidEmbeddedDataSource load(final String jdbcUrl,
            @Nullable Properties properties) throws IOException, TranslatorException,
            ConnectorManagerException, VirtualDatabaseException {

        properties = TeiidEmbeddedUtils.parseJdbcUrl(jdbcUrl, properties);

        final String vdbName = properties.getProperty(TeiidEmbeddedUtils.JDBC_VDB_NAME);
        final String vdbPath = properties.getProperty(TeiidEmbeddedUtils.JDBC_VDB_PATH, "vdb.ddl");

        final HashCode hash = Hashing.murmur3_128().hashString(vdbName + "@" + vdbPath,
                Charsets.UTF_8);

        synchronized (DATA_SOURCES) {
            TeiidEmbeddedDataSource ds = DATA_SOURCES.get(hash);
            if (ds == null) {

                String ddlContent;
                if (Files.exists(Paths.get(vdbPath))) {
                    ddlContent = com.google.common.io.Files.toString(new File(vdbPath),
                            Charsets.UTF_8);
                } else {
                    System.out.println(vdbPath);
                    final URL url = ClassLoader.getSystemClassLoader().getResource(vdbPath);
                    System.out.println(url);
                    ddlContent = Resources.toString(url, Charsets.UTF_8);
                }

                ds = load(ddlContent);

                DATA_SOURCES.put(hash, ds);
            }
            return ds;
        }
    }

    public static TeiidEmbeddedDataSource load(final String ddlContent) throws IOException,
            TranslatorException, ConnectorManagerException, VirtualDatabaseException {

        final HashCode hash = Hashing.murmur3_128().hashString(ddlContent, Charsets.UTF_8);

        synchronized (DATA_SOURCES) {
            TeiidEmbeddedDataSource ds = DATA_SOURCES.get(hash);
            if (ds == null) {
                ds = new TeiidEmbeddedDataSource(ddlContent);
                DATA_SOURCES.put(hash, ds);
            }
            return ds;
        }
    }

    // MAIN METHODS

    @Override
    public Connection getConnection(final String username, final String password)
            throws SQLException {
        return getConnection();
    }

    @Override
    public Connection getConnection() throws SQLException {
        synchronized (this.server) {
            final Driver driver = this.server.getDriver();
            return driver.connect(this.url, null);
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (this.server) {
            this.server.stop();
            for (final Entry<String, Object> e : this.connectionFactories.entrySet()) {
                if (e.getValue() instanceof Closeable) {
                    try {
                        ((Closeable) e.getValue()).close();
                    } catch (final Throwable ex) {
                        LOGGER.warn("Ignoring exception while closing connection factory "
                                + e.getKey());
                    }
                }
            }
            this.connectionFactories.clear();
        }
    }

    // ADDITIONAL METHODS (implementation based on Spring AbstractDataSource)

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        throw new SQLException("DataSource of type [" + getClass().getName()
                + "] cannot be unwrapped as [" + iface.getName() + "]");
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0; // indicates default system timeout
    }

    @Override
    public void setLoginTimeout(final int timeout) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout");
    }

    @Override
    public PrintWriter getLogWriter() {
        throw new UnsupportedOperationException("getLogWriter");
    }

    @Override
    public void setLogWriter(final PrintWriter pw) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter");
    }

    @Override
    public java.util.logging.Logger getParentLogger() {
        return java.util.logging.Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
    }

}