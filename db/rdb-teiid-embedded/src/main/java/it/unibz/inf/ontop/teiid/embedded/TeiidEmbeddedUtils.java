package it.unibz.inf.ontop.teiid.embedded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teiid.core.util.PropertiesUtils;
import org.teiid.jdbc.BaseDataSource;
import org.teiid.net.TeiidURL;
import org.teiid.resource.api.ConnectionFactory;
import org.teiid.translator.ExecutionFactory;
import org.teiid.translator.Translator;
import org.teiid.translator.TranslatorException;

public class TeiidEmbeddedUtils {

    public static final String JDBC_VDB_NAME = BaseDataSource.VDB_NAME;

    public static final String JDBC_VDB_PATH = TeiidURL.CONNECTION.SERVER_URL;

    private static final Logger LOGGER = LoggerFactory.getLogger(TeiidEmbeddedUtils.class);

    private static final Pattern URL_PATTERN = Pattern
            .compile("jdbc:teiid:@ontop:([^@^;]+)(?:@([^;]*))?(;.*)?\"");

    private static final Map<String, Class<?>> EXECUTION_FACTORY_CLASSES;

    private static final Map<String, ExecutionFactory<?, ?>> EXECUTION_FACTORIES = Maps
            .newHashMap();

    static {
        // Scan classpath resource files registering ExecutionFactory classes via ServiceLoader
        // mechanism, and for each class read its @Translator annotation to get the name
        final String resourceName = "META-INF/services/org.teiid.translator.ExecutionFactory";
        final ImmutableMap.Builder<String, Class<?>> builder = ImmutableMap.builder();
        try {
            for (final Enumeration<URL> e = ClassLoader.getSystemClassLoader()
                    .getResources(resourceName); e.hasMoreElements();) {
                final URL url = e.nextElement();
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream(), Charsets.UTF_8))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        final int index = line.indexOf('#');
                        line = (index < 0 ? line : line.substring(0, index)).trim();
                        if (!line.isEmpty()) {
                            try {
                                final Class<?> clazz = Class.forName(line);
                                final String name = clazz.getAnnotation(Translator.class).name();
                                if (ExecutionFactory.class.isAssignableFrom(clazz)) {
                                    builder.put(name, clazz);
                                }
                            } catch (final Throwable ex) {
                                LOGGER.warn("Ignoring invalid ExecutionFactory class {}", line);
                            }
                        }
                    }
                } catch (final Throwable ex) {
                    LOGGER.warn("Ignoring invalid service file {}", url);
                }
            }
        } catch (final IOException ex) {
            throw new Error(ex);
        }
        EXECUTION_FACTORY_CLASSES = builder.build();
    }

    public static boolean acceptsJdbcUrl(final String jdbcUrl) {
        return URL_PATTERN.matcher(jdbcUrl).matches();
    }

    public static Properties parseJdbcUrl(String jdbcUrl, @Nullable Properties properties) {

        jdbcUrl = jdbcUrl.trim();
        final Matcher m = URL_PATTERN.matcher(jdbcUrl);
        if (!m.matches()) {
            throw new IllegalArgumentException();
        }

        try {
            final String vdbName = URLDecoder.decode(m.group(1), "UTF-8");
            final String vdbPath = URLDecoder.decode(m.group(2), "UTF-8");
            final String options = m.group(3);

            properties = properties != null ? PropertiesUtils.clone(properties) : new Properties();
            properties.setProperty(JDBC_VDB_NAME, vdbName);
            properties.setProperty(JDBC_VDB_PATH, vdbPath);
            for (final String option : Splitter.on(';').trimResults().omitEmptyStrings()
                    .split(options)) {
                final int i = option.indexOf('=');
                final String name = URLDecoder.decode( //
                        i < 0 ? option : option.substring(0, i).trim(), "UTF-8");
                final String value = URLDecoder.decode( //
                        i < 0 ? "true" : option.substring(i + 1).trim(), "UTF-8");
                if (!properties.containsKey(name)) {
                    properties.setProperty(name, value);
                }
            }
            return properties;

        } catch (final UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static Object getConnectionFactory(final String url) {

        if (url.startsWith("jdbc:")) {
            final HikariConfig dbConfig = new HikariConfig();
            dbConfig.setJdbcUrl(url);
            return new HikariDataSource(dbConfig);

        } else if (url.startsWith("cf:")) {
            final String cfClassName = url.substring(3);
            try {
                final Class<?> cfClass = Class.forName(cfClassName);
                if (!ConnectionFactory.class.isAssignableFrom(cfClass)) {
                    throw new IllegalArgumentException("Class mentioned in URL " + url
                            + " does not implement " + ConnectionFactory.class.getName());
                }
                return cfClass.newInstance();
            } catch (final ClassNotFoundException ex) {
                throw new IllegalArgumentException(
                        "Class mentioned in URL " + url + " does not exist", ex);
            } catch (IllegalAccessException | InstantiationException ex) {
                throw new IllegalArgumentException(
                        "Class mentioned in URL " + url + " cannot be instantiated", ex);
            }
        }

        throw new IllegalArgumentException("Unsupported URL format: " + url);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ExecutionFactory<?, ?> getExecutionFactory(final String name)
            throws TranslatorException {

        Objects.requireNonNull(name);
        synchronized (EXECUTION_FACTORIES) {
            ExecutionFactory<?, ?> ef = EXECUTION_FACTORIES.get(name);
            if (ef == null) {
                try {
                    final Class<? extends ExecutionFactory<?, ?>> efClass;
                    efClass = (Class) EXECUTION_FACTORY_CLASSES.get(name);
                    ef = efClass.newInstance();
                    ef.start();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new TranslatorException(e);
                }
                EXECUTION_FACTORIES.put(name, ef);
            }
            return ef;
        }
    }

}
