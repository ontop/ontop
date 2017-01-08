package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.OntopModelProperties;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;
import it.unibz.inf.ontop.pivotalrepr.utils.impl.CachingExecutorRegistry;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OntopModelConfigurationImpl implements OntopModelConfiguration {

    private final OntopModelConfigurationOptions options;
    private final OntopModelProperties properties;
    private ExecutorRegistry executorRegistry;
    private Injector injector;

    protected OntopModelConfigurationImpl(OntopModelProperties properties, OntopModelConfigurationOptions options) {
        this.properties = properties;
        this.options = options;

        // Will be built on-demand
        this.executorRegistry = null;
        this.injector = null;
    }

    @Override
    public ExecutorRegistry getExecutorRegistry() {
        if (executorRegistry == null) {
            executorRegistry = new CachingExecutorRegistry(getInjector(), generateOptimizationConfigurationMap());
        }
        return executorRegistry;
    }

    @Override
    public final Injector getInjector() {
        if (injector == null) {
            injector = Guice.createInjector(buildGuiceModules()
                    .collect(Collectors.toMap(
                            // Group modules per class
                            Module::getClass,
                            m -> m,
                            // Two instances of the same class: takes the first one (both are expected to be equivalent)
                            (m1, m2) -> m1
                    )).values());
        }
        return injector;
    }

    /**
     * Can be overloaded by sub-classes
     */
    protected ImmutableMap<Class<? extends QueryOptimizationProposal>, Class<? extends InternalProposalExecutor>>
        generateOptimizationConfigurationMap() {
        return ImmutableMap.of();
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
    public OntopModelProperties getProperties() {
        return properties;
    }


    /**
     * Groups all the options required by the OntopModelConfiguration.
     *
     */
    public static class OntopModelConfigurationOptions {

        public OntopModelConfigurationOptions() {
        }
    }

    protected static class DefaultOntopModelBuilderFragment<B extends Builder> implements OntopModelBuilderFragment<B> {

        private final B builder;
        private Optional<Boolean> testMode = Optional.empty();

        /**
         * To be called when NOT INHERITING
         */
        protected DefaultOntopModelBuilderFragment(B builder) {
            this.builder = builder;
        }

        /**
         * To be called ONLY by sub-classes
         */
        protected DefaultOntopModelBuilderFragment() {
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
            try {
                URI fileURI = new URI(propertyFilePath);
                String scheme = fileURI.getScheme();
                if (scheme == null) {
                    return propertyFile(new File(fileURI.getPath()));
                }
                else if (scheme.equals("file")) {
                    return propertyFile(new File(fileURI));
                }
                else {
                    throw new InvalidOntopConfigurationException("Currently only local property files are supported.");
                }
            } catch (URISyntaxException e) {
                throw new InvalidOntopConfigurationException("Invalid property file path: " + e.getMessage());
            }
        }

        @Override
        public final B propertyFile(File propertyFile) {
            try {
                Properties p = new Properties();
                p.load(new FileReader(propertyFile));
                return properties(p);

            } catch (IOException e) {
                throw new InvalidOntopConfigurationException("Cannot reach the property file: " + propertyFile);
            }
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
        protected Properties generateUserProperties() {
            Properties properties = new Properties();
            inputProperties.ifPresent(properties::putAll);
            testMode.ifPresent(isEnabled -> properties.put(OntopModelProperties.TEST_MODE, isEnabled));
            return properties;
        }

        protected final OntopModelConfigurationOptions generateOntopModelConfigurationOptions() {
            return new OntopModelConfigurationOptions();
        }

    }

    /**
     * Builder
     *
     */
    public final static class BuilderImpl<B extends Builder> extends DefaultOntopModelBuilderFragment<B>
            implements Builder<B> {

        @Override
        public final OntopModelConfiguration build() {
            Properties p = generateUserProperties();

            return new OntopModelConfigurationImpl(
                    new OntopModelPropertiesImpl(p),
                    generateOntopModelConfigurationOptions());
        }
    }
}
