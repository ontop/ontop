package it.unibz.inf.ontop.teiid.local;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teiid.translator.ExecutionFactory;
import org.teiid.translator.Translator;

import it.unibz.inf.ontop.teiid.util.Pool;
import it.unibz.inf.ontop.teiid.util.Pool.Lease;

public final class ExecutionFactories {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionFactories.class);

    private static final Supplier<Pool<String, ExecutionFactory<?, ?>>> POOL //
            = Suppliers.memoize(() -> Pool.create(n -> create(n)));

    private static final Supplier<Map<String, Class<?>>> CLASSES = Suppliers.memoize(() -> scan());

    public static Lease<ExecutionFactory<?, ?>> lease(final String name) {

        // Check required name
        Objects.requireNonNull(name);

        // Delegate to the pool, instantiating it at first access
        return POOL.get().get(name);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ExecutionFactory<?, ?> create(final String name) {

        // Check mandatory name argument
        Objects.requireNonNull(name);

        // Lookup the ExecutionFactory class registered with the name supplied
        final Class<? extends ExecutionFactory<?, ?>> efClass ///
                = (Class) CLASSES.get().get(name);
        if (efClass == null) {
            throw new IllegalArgumentException(
                    "No ExecutionFactory class registered for name " + name);
        }

        try {
            // Instantiate the ExecutionFactory object
            final ExecutionFactory<?, ?> ef = efClass.newInstance();
            LOGGER.debug("Created ExecutionFactory '{}' (class {})", name, efClass);

            // Start and return the ExecutionFactory object
            ef.start();
            LOGGER.debug("Started ExecutionFactory '{}'", name);
            return ef;

        } catch (final Throwable ex) {
            // Wrap and propagate
            Throwables.throwIfUnchecked(ex);
            throw new RuntimeException(ex);
        }
    }

    private static Map<String, Class<?>> scan() {

        // Store results in an immutable map
        final String path = "META-INF/services/org.teiid.translator.ExecutionFactory";
        final Map<String, Class<?>> classes = Maps.newLinkedHashMap();

        try {
            // Scan classpath resources registering ExecutionFactory classes via ServiceLoader
            final Enumeration<URL> e = ClassLoader.getSystemClassLoader().getResources(path);
            while (e.hasMoreElements()) {

                // Process current resource, skipping it on error
                final URL url = e.nextElement();
                try {
                    try (BufferedReader in = new BufferedReader(
                            new InputStreamReader(url.openStream(), Charsets.UTF_8))) {

                        // Read one line at a time, discarding comments and empty lines
                        String line;
                        while ((line = in.readLine()) != null) {
                            final int index = line.indexOf('#');
                            line = (index < 0 ? line : line.substring(0, index)).trim();
                            if (!line.isEmpty()) {
                                try {
                                    // Load class and check it extends ExecutionFactory
                                    final Class<?> clazz = Class.forName(line);
                                    Preconditions.checkArgument(
                                            ExecutionFactory.class.isAssignableFrom(clazz),
                                            "Class does not extend ExecutionFactory");

                                    // Extract @Translator annotation
                                    final Translator ann = clazz.getAnnotation(Translator.class);
                                    Preconditions.checkArgument(ann != null,
                                            "Class not annotated with @Translator");

                                    // Extract name and verify it was not used before
                                    final String name = ann.name();
                                    final Class<?> oldClass = classes.get(name);
                                    Preconditions.checkArgument(
                                            oldClass == null || oldClass == clazz,
                                            "name %s already bound to class %s", name, oldClass);

                                    // Store the <name, class> pair
                                    if (oldClass != clazz) {
                                        classes.put(name, clazz);
                                        LOGGER.debug("Detected ExecutionFactory class '{}' "
                                                + "for name '{}", clazz, name);
                                    }

                                } catch (final Throwable ex) {
                                    // Ignore line-level errors
                                    LOGGER.warn("Ignoring invalid ExecutionFactory "
                                            + "class specification: {}", line, ex);
                                }
                            }
                        }
                    }

                } catch (final Throwable ex) {
                    // Discard current resource and proceed
                    LOGGER.warn("Ignoring invalid service file {}", url);
                }
            }

        } catch (final IOException ex) {
            // Log and ignore
            LOGGER.warn("Error scanning <name, class> pairs for ExecutionFactory classes from "
                    + path + " classpath resources", ex);
        }

        // Return scan results (possibly incomplete in case of errors)
        return ImmutableMap.copyOf(classes);
    }

    private ExecutionFactories() {
        throw new Error(); // prevent instantiation via reflection
    }

}
