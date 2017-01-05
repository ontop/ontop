package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.OntopModelProperties;
import it.unibz.inf.ontop.pivotalrepr.OptimizationConfiguration;
import it.unibz.inf.ontop.pivotalrepr.impl.OptimizationConfigurationImpl;
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
    private final OptimizationConfiguration optimizationConfiguration;
    private Injector injector;

    protected OntopModelConfigurationImpl(OntopModelProperties properties, OntopModelConfigurationOptions options) {
        this.properties = properties;
        this.options = options;
        this.optimizationConfiguration = new OptimizationConfigurationImpl(generateOptimizationConfigurationMap());
        // Will be built on-demand
        injector = null;
    }

    @Override
    public OptimizationConfiguration getOptimizationConfiguration() {
        return optimizationConfiguration;
    }

    @Override
    public final Injector getInjector() {
        if (injector == null) {
            injector = Guice.createInjector(buildGuiceModules().collect(Collectors.toList()));
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
    public OntopModelProperties getOntopModelProperties() {
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

    /**
     * Builder
     *
     */
    public static class BuilderImpl<B extends Builder, P extends OntopModelProperties, C extends OntopModelConfiguration>
            implements Builder<B> {

        private Optional<Properties> properties = Optional.empty();

        /**
         * Please make sure it is an instance of B!
         */
        public BuilderImpl() {
        }

        /**
         * Have precedence over other parameters
         */
        @Override
        public B properties(@Nonnull Properties properties) {
            this.properties = Optional.of(properties);
            return (B) this;
        }

        @Override
        public B propertyFile(String propertyFilePath) {
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
        public B propertyFile(File propertyFile) {
            try {
                Properties p = new Properties();
                p.load(new FileReader(propertyFile));
                return properties(p);

            } catch (IOException e) {
                throw new InvalidOntopConfigurationException("Cannot reach the property file: " + propertyFile);
            }
        }

        @Override
        public final C build() {
            Properties p = generateProperties();

            /**
             * User-provided properties have the highest precedence.
             */
            properties.ifPresent(p::putAll);

            P ontopProperties = createOntopModelProperties(p);

            return createConfiguration(ontopProperties);
        }

        /**
         * To be overloaded by specialized classes (extensions).
         *
         * Default implementation for P == OntopModelProperties
         */
        protected P createOntopModelProperties(Properties p) {
            return (P) new OntopModelPropertiesImpl(p);
        }

        /**
         * To be overloaded by specialized classes (extensions).
         *
         * Default implementation for P == OntopModelProperties
         */
        protected C createConfiguration(P ontopProperties) {
            return (C) new OntopModelConfigurationImpl(ontopProperties, createOntopModelConfigurationArguments());
        }

        protected final OntopModelConfigurationOptions createOntopModelConfigurationArguments() {
            return new OntopModelConfigurationOptions();
        }

        protected Properties generateProperties() {
            return new Properties();
        }
    }
}
