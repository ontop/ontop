package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class OntopModelConfigurationImpl implements OntopModelConfiguration {

    private final OntopModelConfigurationOptions options;
    @Nullable
    private final Supplier<Injector> injectorSupplier;
    private final OntopModelSettings settings;
    @Nullable
    private Injector injector;


    protected OntopModelConfigurationImpl(@Nonnull OntopModelSettings settings, @Nonnull OntopModelConfigurationOptions options) {
        this.settings = settings;
        this.options = options;
        this.injector = null;
        this.injectorSupplier = null;
    }

    /**
     * "Slave" configuration (in case of multiple inheritance)
     *  {@code -->} uses the injector of another configuration
     */
    protected OntopModelConfigurationImpl(@Nonnull OntopModelSettings settings, @Nonnull OntopModelConfigurationOptions options,
                                          @Nonnull Supplier<Injector> injectorSupplier) {
        this.settings = settings;
        this.options = options;
        this.injectorSupplier = injectorSupplier;

        this.injector = null;
    }

    @Override
    public final Injector getInjector() {
        if (injector == null) {
            /*
             * When the configuration is a "slave"
             */
            if (injectorSupplier != null) {
                injector = injectorSupplier.get();
            }
            else {
                Set<Class> moduleClasses = new HashSet<>();

                // Only keeps the first instance of a module class
                ImmutableList<Module> modules = buildGuiceModules()
                        .filter(m -> moduleClasses.add(m.getClass()))
                        .collect(ImmutableCollectors.toList());

                injector = Guice.createInjector(modules);
            }
        }
        return injector;
    }

    /**
     * To be overloaded
     *
     */
    protected Stream<Module> buildGuiceModules() {
        return Stream.of(new OntopModelModule(this));
    }

    /**
     * To be overloaded
     */
    @Override
    public void validate() throws InvalidOntopConfigurationException {
    }

    @Override
    public IntermediateQueryFactory getIQFactory() {
        return getInjector().getInstance(IntermediateQueryFactory.class);
    }

    @Override
    public AtomFactory getAtomFactory() {
        return getInjector().getInstance(AtomFactory.class);
    }

    @Override
    public TermFactory getTermFactory() {
        return getInjector().getInstance(TermFactory.class);
    }

    @Override
    public TypeFactory getTypeFactory() {
        return getInjector().getInstance(TypeFactory.class);
    }

    @Override
    public RDF getRdfFactory() {
        return getInjector().getInstance(RDF.class);
    }

    @Override
    public OntopModelSettings getSettings() {
        return settings;
    }


    /**
     * Groups all the options required by the OntopModelConfiguration.
     *
     */
    public static class OntopModelConfigurationOptions {

        public OntopModelConfigurationOptions() {
        }
    }

    protected static class DefaultOntopModelBuilderFragment<B extends Builder<B>> implements OntopModelBuilderFragment<B> {

        private final B builder;
        private Optional<Boolean> testMode = Optional.empty();

        /**
         * To be called when NOT INHERITING
         */
        protected DefaultOntopModelBuilderFragment(B builder) {
            this.builder = builder;
        }

        /**
         * To be called ONLY by the local BuilderImpl static class
         */
        private DefaultOntopModelBuilderFragment() {
            this.builder = (B) this;
        }


        private Optional<Properties> inputProperties = Optional.empty();

        /**
         * Have precedence over other parameters
         */
        @Override
        public final B properties(@Nonnull Properties properties) {
            this.inputProperties = Optional.of(properties);
            return builder;
        }

        @Override
        public final B propertyFile(String propertyFilePath) {
            return propertyFile(extractPropertyFile(propertyFilePath));
        }

        @Override
        public final B propertyFile(File propertyFile) {
            return properties(extractProperties(propertyFile));
        }

        @Override
        public B enableTestMode() {
            testMode = Optional.of(true);
            return builder;
        }

        /**
         *
         * Derived properties have the highest precedence over input properties.
         *
         * Can be overloaded. Don't forget to call the parent!
         *
         */
        protected Properties generateProperties() {
            Properties properties = new Properties();
            inputProperties.ifPresent(properties::putAll);
            testMode.ifPresent(isEnabled -> properties.put(OntopModelSettings.TEST_MODE, isEnabled));
            return properties;
        }

        protected final OntopModelConfigurationOptions generateModelOptions() {
            return new OntopModelConfigurationOptions();
        }

    }

    /**
     * Builder
     *
     */
    public final static class BuilderImpl<B extends Builder<B>> extends DefaultOntopModelBuilderFragment<B>
            implements Builder<B> {

        @Override
        public final OntopModelConfiguration build() {
            Properties p = generateProperties();

            return new OntopModelConfigurationImpl(
                    new OntopModelSettingsImpl(p),
                    generateModelOptions());
        }
    }

    public static File extractPropertyFile(String propertyFilePath) {
        try {
            URI fileURI = new URI(propertyFilePath);
            String scheme = fileURI.getScheme();
            if (scheme == null) {
                return new File(fileURI.getPath());
            }
            else if (scheme.equals("file")) {
                return new File(fileURI);
            }
            else {
                throw new InvalidOntopConfigurationException("Currently only local property files are supported.");
            }
        } catch (URISyntaxException e) {
            throw new InvalidOntopConfigurationException("Invalid property file path: " + e.getMessage());
        }
    }

    public static Properties extractProperties(File propertyFile) throws InvalidOntopConfigurationException {
        try (FileReader reader  = new FileReader(propertyFile)) {
            Properties p = new Properties();
            p.load(reader);
            return p;
        }
        catch (IOException e) {
            throw new InvalidOntopConfigurationException("Cannot reach the property file: " + propertyFile);
        }
    }
}
