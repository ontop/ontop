package it.unibz.inf.ontop.teiid.local;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teiid.resource.api.ConnectionFactory;

import it.unibz.inf.ontop.teiid.util.Pool;
import it.unibz.inf.ontop.teiid.util.Pool.Lease;

public final class ConnectionFactories {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionFactories.class);

    private static final Supplier<Pool<List<?>, Object>> POOL = Suppliers
            .memoize(() -> Pool.create(list -> {
                final String u = (String) list.get(0);
                final Properties p = (Properties) list.get(1);
                return create(u, p);
            }));

    public static Lease<Object> lease(final String url, @Nullable Properties properties) {

        // Check required URL argument
        Objects.requireNonNull(url);

        // Clone properties to protect from external changes, as they may be indexed in the pool
        properties = properties == null ? new Properties() : (Properties) properties.clone();

        // Delegate to the pool, which is instantiated at first access
        return POOL.get().get(Arrays.asList(url, properties));
    }

    public static Object create(final String url, @Nullable Properties properties) {

        // Allocate empty properties, if the input is null
        properties = properties != null ? properties : new Properties();

        // Delegate to separate methods based on URL protocol (jdbc: / cf:)
        final Object result;
        if (url.startsWith("jdbc:")) {
            result = createJdbc(url, properties);
        } else if (url.startsWith("cf:")) {
            result = createCf(url, properties);
        } else {
            throw new IllegalArgumentException(
                    "Invalid URL '" + url + "': unrecognized URL format");
        }

        // Log and return result
        LOGGER.debug("Created Teiid connection factory for URL '{}' and properties {}: {}", url,
                properties, result);
        return result;
    }

    private static DataSource createJdbc(final String url, final Properties properties) {

        // Setup Hikari pool configuration based on JDBC url and optional properties
        final HikariConfig dbConfig = new HikariConfig(properties);
        dbConfig.setJdbcUrl(url);

        // Creates the Hikari pool
        return new HikariDataSource(dbConfig);
    }

    private static Object createCf(final String url, final Properties properties) {

        // Extract the 'class' or 'class.method' target from the URL
        final int index = url.indexOf(':', 3);
        final String target = url.substring(3, index < 0 ? url.length() : index);

        // Distinguish between 'class' and 'class.method', by checking if 'class' exists
        Executable executable = null;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(target);
        } catch (final ClassNotFoundException ex) {
            // Ignore
        }

        try {
            // Gather candidate constructors / static factory methods
            final Iterable<? extends Executable> candidates;
            if (clazz != null) {
                // For 'class' case, we check class is concrete and implements ConnectionFactory
                final int mods = clazz.getModifiers();
                Preconditions.checkArgument(
                        !Modifier.isAbstract(mods) && !Modifier.isInterface(mods),
                        "Class cannot be instantiated");
                Preconditions.checkArgument(ConnectionFactory.class.isAssignableFrom(clazz),
                        "Class does not implement org.teiid.resource.api.ConnectionFactory");

                // We consider as candidates all public constructors as candidates
                candidates = Arrays.asList(clazz.getConstructors());

            } else {
                try {
                    // For 'class.method', we first obtain class name and method name
                    final int idx = target.lastIndexOf('.');
                    final String clazzName = target.substring(0, idx);
                    final String methodName = target.substring(idx + 1);
                    clazz = Class.forName(clazzName);

                    // We consider as candidates all static methods matching name and return type
                    candidates = Iterables.filter(Arrays.asList(clazz.getMethods()),
                            m -> ConnectionFactory.class.isAssignableFrom(m.getReturnType()) //
                                    && Modifier.isStatic(m.getModifiers()) //
                                    && m.getName().equals(methodName));

                } catch (final Throwable ex) {
                    throw new IllegalArgumentException(
                            "Could not find matching class or class/method for " + target);
                }
            }

            // Select the candidate to execute, based on arguments (prefer higher # of arguments)
            for (final Executable e : candidates) {
                final Class<?>[] types = e.getParameterTypes();
                if (types.length <= 2
                        && (types.length < 1 || types[0].equals(String.class)
                                || types[0].equals(Properties.class))
                        && (types.length < 2 || types[1].equals(String.class)
                                || types[1].equals(Properties.class))
                        && (types.length < 2 || !types[0].equals(types[1]))) {
                    if (executable == null
                            || executable.getParameterTypes().length < types.length) {
                        executable = e;
                    }
                }
            }
            if (executable == null) {
                throw new IllegalArgumentException(
                        "No suitable public constructor / static factory method. Candidates: "
                                + candidates);
            }

        } catch (final Throwable ex) {
            // All errors above denote a wrong target in supplied URL
            throw new IllegalArgumentException("Invalid URL " + url + ": " + ex.getMessage(), ex);
        }

        // Build argument lists
        final Class<?>[] types = executable.getParameterTypes();
        final Object[] args = new Object[types.length];
        for (int i = 0; i < args.length; ++i) {
            args[i] = types[i].equals(String.class) ? url : properties;
        }

        try {
            // Invoke constructor / method, in the latter case checking that result is not null
            if (executable instanceof Constructor<?>) {
                return ((Constructor<?>) executable).newInstance(args);
            } else {
                final Object cf = ((Method) executable).invoke(null, args);
                Preconditions.checkArgument(cf != null, "Static method returned null");
                return cf;
            }

        } catch (final IllegalAccessException | InstantiationException ex) {
            // Interpret this error as the URL referring to invalid class/method pair
            throw new IllegalArgumentException(
                    "Invalid URL " + url + ": cannot access constructor/method", ex);

        } catch (final InvocationTargetException ex) {
            // Unwrap constructor/method error and propagate as unchecked exception
            Throwables.throwIfUnchecked(ex.getCause());
            throw new RuntimeException(ex.getCause()); 
        }
    }

    private ConnectionFactories() {
        throw new Error(); // prevent instantiation via reflection
    }

}