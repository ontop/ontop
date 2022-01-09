package it.unibz.inf.ontop.teiid.local;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teiid.adminapi.Model;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.adminapi.impl.SourceMappingMetadata;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.adminapi.impl.VDBMetadataParser;
import org.teiid.adminapi.impl.VDBTranslatorMetaData;
import org.teiid.cache.caffeine.CaffeineCacheFactory;
import org.teiid.core.util.ObjectConverterUtil;
import org.teiid.deployers.VDBRepository;
import org.teiid.deployers.VirtualDatabaseException;
import org.teiid.jdbc.BaseDataSource;
import org.teiid.metadatastore.DeploymentBasedDatabaseStore;
import org.teiid.query.metadata.NioZipFileSystem;
import org.teiid.query.metadata.VDBResources;
import org.teiid.query.metadata.VirtualFile;
import org.teiid.runtime.EmbeddedConfiguration;
import org.teiid.translator.ExecutionFactory;
import org.teiid.transport.SSLConfiguration;
import org.teiid.transport.SocketConfiguration;

import it.unibz.inf.ontop.teiid.util.Pool;
import it.unibz.inf.ontop.teiid.util.Pool.Lease;
import it.unibz.inf.ontop.teiid.util.Text;

public final class LocalServers {

    private static final String KEY_VDB_NAME = BaseDataSource.VDB_NAME;

    private static final String KEY_VDB_PATH = "vdb";

    private static final String KEY_EXPORT = "export";

    private static final String KEY_KEYSTORE = "keystore";

    private static final String KEY_FULL_DDL = VDBMetaData.TEIID_DDL;

    private static final String KEY_FULL_XML = "full-xml";

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalServers.class);

    private static final Pattern PATTERN_EXPORT = Pattern
            .compile("(?:(teiid|pg):)?(?:([^:]*[^0-9][^:]*):)?([0-9]+)(?::([^:]+))?");

    private static final Supplier<Pool<Properties, LocalServer>> POOL = Suppliers
            .memoize(() -> Pool.create(properties -> create(properties)));

    static {
        try {
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

            // Configure transaction manager storage location (needed also for in-memory store)
            final Path tmp = Files.createTempDirectory("narayana-obect-store-");
            tmp.toFile().deleteOnExit();
            System.setProperty("ObjectStoreEnvironmentBean.objectStoreDir", tmp.toString());
            System.setProperty("com.arjuna.ats.arjuna.objectstore.objectStoreDir", tmp.toString());

        } catch (final Throwable ex) {
            // Unexpected
            throw new Error(ex);
        }
    }

    public static Lease<LocalServer> lease(Properties properties) {

        // Check required URL argument
        Objects.requireNonNull(properties);

        // Clone properties to protect from external changes, as they may be indexed in the pool
        properties = (Properties) properties.clone();

        // Delegate to the pool, which is instantiated at first access
        return POOL.get().get(properties);
    }

    public static LocalServer create(final Properties jdbcProperties) {

        // Check argument
        Objects.requireNonNull(jdbcProperties);

        // Merge properties: JDBC properties > system properties > env vars (lower preference)
        Properties props = mergeProps(System.getenv(), System.getProperties(), jdbcProperties);

        // Load VDB definition into a VDBMetaData object
        final VDBMetaData metadata = loadVDBMetadata(props);

        // Remerge properties: JDBC properties > VDB definition > system properties > env vars
        props = mergeProps(System.getenv(), System.getProperties(), metadata.getProperties(),
                jdbcProperties);

        // Log beginning of creation process
        final long ts = System.currentTimeMillis();
        final String vdbName = props.getProperty(KEY_VDB_NAME);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Teiid server for VDB {} starting (source {})", vdbName,
                    props.getProperty(KEY_VDB_PATH));
        }

        // Keep track of allocated server and sub-components, so to release them when done
        LocalServer server = null;
        final List<Lease<?>> leases = Lists.newArrayList();

        try {
            // Create and configure server configuration object
            final EmbeddedConfiguration config = new EmbeddedConfiguration();
            configureTransports(config, props);
            config.setCacheFactory(new CaffeineCacheFactory());
            config.setTransactionManager(
                    com.arjuna.ats.jta.TransactionManager.transactionManager()); // TODO
            // TODO SecurityHelper, security domain, authenticationType
            // TODO useDisk, bufferDirectory

            // Create server object, registering close callback to release sub-components
            server = new LocalServer(s -> {
                for (final Lease<?> lease : leases) {
                    lease.close();
                }
                LOGGER.info("Teiid server for VDB {} terminated", vdbName);
            });

            // Configure server components
            configureExecutionFactories(server, metadata, leases);
            configureConnectionFactories(server, metadata, props, leases);

            // Start the server, using the configuration object previously created
            server.start(config);

            // Deploy the VDB (note: have to parse the DDL/XML again due to need of populating a
            // VDBRepository internal to the server)
            final boolean vdbDdl = metadata.getPropertyValue(KEY_FULL_DDL) != null;
            final String vdbText = metadata.getPropertyValue(vdbDdl ? KEY_FULL_DDL : KEY_FULL_XML);
            server.deployVDB(new ByteArrayInputStream(vdbText.getBytes(Charsets.UTF_8)), vdbDdl);

            // Log completion
            LOGGER.info("Teiid server for VDB {} started ({} ms)", vdbName,
                    System.currentTimeMillis() - ts);

            // Return created server, which *needs* to be closed for sub-components to be released
            return server;

        } catch (final Throwable ex) {

            // Log failure
            LOGGER.info("Teiid server for VDB {} creation failed: {}", vdbName, ex.getMessage());

            // Stop and close server, if needed (errors appended to main exception)
            if (server != null) {
                try {
                    server.stop();
                } catch (final Throwable ex2) {
                    ex.addSuppressed(ex2);
                }
                try {
                    server.close();
                } catch (final Throwable ex3) {
                    ex.addSuppressed(ex3);
                }
            }

            // Release sub-components on error (possibly done by server.close())
            for (final Lease<?> lease : leases) {
                lease.close();
            }

            // Propagate
            Throwables.throwIfUnchecked(ex);
            throw new RuntimeException(ex);
        }
    }

    private static void configureTransports(final EmbeddedConfiguration config,
            final Properties properties) {

        // Abort if property KEY_EXPORT was not supplied (i.e., no ports to export)
        final String export = Strings.nullToEmpty(properties.getProperty(KEY_EXPORT)).trim();
        if (export.isEmpty()) {
            return;
        }

        // Retrieve keystore file name and password from property KEY_KEYSTORE, if supplied
        final String ksSpec = Strings.nullToEmpty(properties.getProperty(KEY_KEYSTORE)).trim();
        final List<String> ksTokens = Splitter.on(":").trimResults().splitToList(ksSpec);
        Preconditions.checkArgument(ksSpec.isEmpty() || ksTokens.size() == 2);
        final String ksFilename = ksSpec.isEmpty() ? null : ksTokens.get(0);
        final String ksPassword = ksSpec.isEmpty() ? null : ksTokens.get(1);
        if (ksFilename != null) {
            LOGGER.debug("Using keystore at {}", ksFilename);
        }

        // Iterate over all comma-separated port specifications in property KEY_EXPORT
        for (final String spec : Splitter.on(',').omitEmptyStrings().trimResults().split(export)) {

            // Parse the port specification
            final Matcher m = PATTERN_EXPORT.matcher(spec);
            Preconditions.checkArgument(m.matches(), "Invalid export specification %s", spec);

            // Setup basic configuration: protocol (teiid / pg), bind address, port
            final SocketConfiguration sc = new SocketConfiguration();
            sc.setProtocol(MoreObjects.firstNonNull(m.group(1), "teiid"));
            sc.setBindAddress(MoreObjects.firstNonNull(m.group(2), "0.0.0.0"));
            sc.setPortNumber(Integer.parseInt(m.group(3)));

            // Setup SSL keystore, if a key alias was included in the specification
            if (m.group(4) != null) {
                Preconditions.checkArgument(ksFilename != null, "Use of SSL key '%s' in '%s' "
                        + "requires specifying keystore filename and password via property '%s'",
                        m.group(4), spec, KEY_KEYSTORE);
                final SSLConfiguration sslConfig = new SSLConfiguration();
                sslConfig.setMode(SSLConfiguration.ENABLED);
                sslConfig.setDisableTrustManager(true);
                sslConfig.setKeystoreFilename(ksFilename);
                sslConfig.setKeystorePassword(ksPassword);
                sslConfig.setKeystoreKeyAlias(m.group(4));
                sc.setSSLConfiguration(sslConfig);
            }

            // Append the port to the list of ports to be exposed
            config.addTransport(sc);
            LOGGER.debug("Using exported {} endpoint at {}:{} (SSL key {})", sc.getProtocol(),
                    sc.getHostName(), sc.getPortNumber(), m.group(4));
        }
    }

    private static void configureExecutionFactories(final LocalServer server,
            final VDBMetaData metadata, final List<Lease<?>> leases) {

        // Collect names of non-override translators mentioned in the DDL
        // Metadata for override translator is already registered, so no need to manipulate it
        final Set<String> names = Sets.newHashSet();
        for (final ModelMetaData model : metadata.getModelMetaDatas().values()) {
            for (final SourceMappingMetadata mapping : model.getSourceMappings()) {
                final String name = mapping.getTranslatorName();
                final VDBTranslatorMetaData meta = metadata.getTranslator(name);
                if (meta == null || meta.getType() == null) {
                    names.add(name);
                }
            }
        }

        // Register execution factories for each non-override translator, keeping track of leases
        for (final String name : names) {
            final Lease<ExecutionFactory<?, ?>> lease = ExecutionFactories.lease(name);
            leases.add(lease);
            final ExecutionFactory<?, ?> executionFactory = lease.get();
            server.addTranslator(name, executionFactory);
            LOGGER.debug("Using execution factory '{}'", name, executionFactory);
        }
    }

    private static void configureConnectionFactories(final LocalServer server,
            final VDBMetaData metadata, final Properties properties, final List<Lease<?>> leases) {

        // Collect source names mentioned in the VDB definition
        final Set<String> names = Sets.newHashSet();
        for (final ModelMetaData model : metadata.getModelMetaDatas().values()) {
            if (model.getModelType() == Model.Type.PHYSICAL) {
                for (final SourceMappingMetadata mapping : model.getSourceMappings()) {
                    names.add(mapping.getName());
                }
            }
        }

        // Register a connection factory for each source with associated properties
        for (final String name : names) {

            // Retrieve the connection factory URL using property server.<name>
            final String url = properties.getProperty("server." + name);
            Preconditions.checkArgument(url != null, "No URL supplied for server %s", name);

            // Retrieve the connection factory properties using props. server.<name>.<prop>
            final Properties props = new Properties();
            final String prefix = "server." + name + ".";
            for (final Entry<?, ?> e : properties.entrySet()) {
                final String key = (String) e.getKey();
                if (key.startsWith(prefix)) {
                    final String value = (String) e.getValue();
                    props.setProperty(key.substring(prefix.length()), value);
                }
            }

            // Allocate & register the connection factory, keeping track of its lease so to
            // release it at server close time (or if an error occur when creating the server)
            final Lease<Object> lease = ConnectionFactories.lease(url, props);
            leases.add(lease);
            final Object connectionFactory = lease.get();
            server.addConnectionFactory(name, connectionFactory);
            LOGGER.debug("Using connection factory '{}': {}", name, connectionFactory);
        }
    }

    private static VDBMetaData loadVDBMetadata(final Properties properties) {

        // Identify a URL from where to load the VDB definition, looking first at the
        // user-specified KEY_VDB_PATH property and after at default locations
        URL vdbResource = null;
        final String[] locations = new String[] { properties.getProperty(KEY_VDB_PATH),
                "teiid.vdb", "teiid.ddl", "teiid.xml" };
        for (int i = 0; vdbResource == null && i < locations.length; ++i) {
            final String location = locations[i];
            if (location != null) {
                final Path path = Paths.get(location);
                try {
                    vdbResource = Files.exists(path) ? path.toUri().toURL()
                            : ClassLoader.getSystemClassLoader().getResource(location);
                } catch (final MalformedURLException ex) {
                    // Ignore and treat as resource missing
                }
                Preconditions.checkArgument(vdbResource != null || i > 0,
                        "Could not find VDB file / classpath resource at %s", location);
            }
        }

        // Fail if the VDB definition was not found
        Preconditions.checkArgument(vdbResource != null, "No VDB definition found");

        // Load the identified VDB definition (ddl, xml, zip)
        try {
            try (InputStream in = vdbResource.openStream()) {
                final String url = vdbResource.toString();
                final VDBMetaData result = loadVDBMetadata(properties, url, in);
                LOGGER.debug("Using VDB from {}", url);
                return result;
            }
        } catch (final Exception ex) {
            throw new IllegalArgumentException(
                    "Could not load VDB definition from " + vdbResource.getPath(), ex);
        }
    }

    private static VDBMetaData loadVDBMetadata(final Properties properties, final String url,
            final InputStream stream) throws Exception {

        // Ensure the supplied stream is buffered
        final BufferedInputStream in = stream instanceof BufferedInputStream
                ? (BufferedInputStream) stream
                : new BufferedInputStream(stream);

        // Check first4 bytes to determine whether the file is a ZIP (requires a buffered stream)
        boolean isZip = false;
        in.mark(4);
        try {
            final byte[] h = new byte[4];
            ByteStreams.readFully(in, h);
            isZip = h[0] == 0x50 && h[1] == 0x4B && h[2] == 0x03 && h[3] == 0x04;
        } catch (final EOFException ex) {
            // Ignore (file shorter than 4 bytes), will treat as non-ZIP
        }
        in.reset();

        // Differentiate non-ZIP VS ZIP cases
        if (!isZip) {
            // non-ZIP case: read the file to string, substituting variable placeholders
            String content = ObjectConverterUtil.convertToString(in);
            content = Text.substitute(content, properties, System.getProperties());

            // Treat .xml files as XML VDB definitions, everything else (e.g., .vdb & .ddl) as DDL
            if (url.endsWith(".xml")) {
                // XML VDB definition: validate and parse
                final byte[] bytes = content.getBytes(Charsets.UTF_8);
                VDBMetadataParser.validate(new ByteArrayInputStream(bytes));
                final VDBMetaData result = VDBMetadataParser
                        .unmarshall(new ByteArrayInputStream(bytes));
                result.addProperty(KEY_FULL_XML, content);
                return result;

            } else {
                // DDL VDB definition: parse
                final DeploymentBasedDatabaseStore store;
                store = new DeploymentBasedDatabaseStore(new VDBRepository());
                final VDBMetaData result = store.getVDBMetadata(content);
                result.addProperty(KEY_FULL_DDL, content);
                return result;
            }

        } else {
            // Mount the ZIP file as a "VirtualFile". We reuse the file URL backing the received
            // stream if possible, otherwise (e.g., URL refers to some remote resource or a JAR
            // classpath resource) we clone the resource to a temporary file
            final VirtualFile root;
            if (url.startsWith("file://")) {
                root = NioZipFileSystem.mount(new URL(url));
            } else {
                final File f = File.createTempFile("teiid-vdb-", null);
                f.deleteOnExit();
                ObjectConverterUtil.write(in, f);
                root = NioZipFileSystem.mount(f.toURI().toURL());
            }

            // Identify and load the VDB definition within the ZIP, looking at default locations
            for (final String zipPath : new String[] { "/vdb.xml", "/META-INF/vdb.xml", "/vdb.ddl",
                    "/META-INF/vdb.ddl" }) {
                final VirtualFile zipFile = root.getChild(zipPath);
                if (zipFile.exists()) {
                    try (InputStream zipIn = zipFile.openStream()) {
                        final VDBMetaData result = loadVDBMetadata(properties, zipPath, zipIn);
                        result.addAttachment(VirtualFile.class, root);
                        final VDBResources resources = new VDBResources(root);
                        result.addAttachment(VDBResources.class, resources);
                        return result;
                    }
                }
            }

            // Fail in case a VDB definition could not be found at any of the default locations
            throw new VirtualDatabaseException(
                    "Could not find a vdb.xml or vdb.ddl file in " + url);
        }
    }

    private static Properties mergeProps(final Map<?, ?>... lowToHighPrecedenceProps) {
        final Properties result = new Properties();
        for (final Map<?, ?> p : lowToHighPrecedenceProps) {
            result.putAll(p);
        }
        return result;
    }

    private LocalServers() {
        throw new Error(); // prevent instantiation via reflection
    }

}
