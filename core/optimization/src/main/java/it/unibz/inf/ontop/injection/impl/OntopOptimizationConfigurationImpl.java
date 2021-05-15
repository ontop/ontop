package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopOptimizationConfiguration;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Properties;
import java.util.stream.Stream;

public class OntopOptimizationConfigurationImpl extends OntopModelConfigurationImpl
        implements OntopOptimizationConfiguration {

    protected OntopOptimizationConfigurationImpl(OntopOptimizationSettings settings, OntopOptimizationOptions options) {
        super(settings, options.getModelOptions());
    }

    public static class OntopOptimizationOptions {
        private final OntopModelConfigurationOptions modelOptions;

        OntopOptimizationOptions(OntopModelConfigurationOptions modelOptions) {
            this.modelOptions = modelOptions;
        }

        public OntopModelConfigurationOptions getModelOptions() {
            return modelOptions;
        }
    }

    @Override
    public OntopOptimizationSettings getSettings() {
        return (OntopOptimizationSettings) super.getSettings();
    }

    /**
     * To be overloaded
     *
     */
    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(new OntopOptimizationModule(this)));
    }

    protected static class DefaultOntopOptimizationBuilderFragment<B extends OntopOptimizationConfiguration.Builder<B>>
            implements OntopOptimizationBuilderFragment<B> {

        private final B builder;

        /**
         * For sub-classes ONLY!
         */
        protected DefaultOntopOptimizationBuilderFragment() {
            builder = (B) this;
        }

        /**
         * When not inheriting
         */
        protected DefaultOntopOptimizationBuilderFragment(B builder) {
            this.builder = builder;
        }

        protected Properties generateProperties() {
            return new Properties();
        }

        protected final OntopOptimizationOptions generateOptimizationOptions(
                OntopModelConfigurationOptions modelOptions) {
            return new OntopOptimizationOptions(modelOptions);
        }

    }

    protected static abstract class AbstractOntopOptimizationBuilderMixin<B extends OntopOptimizationConfiguration.Builder<B>>
            implements OntopOptimizationConfiguration.Builder<B> {

        private final DefaultOntopOptimizationBuilderFragment<B> optimizationBuilderFragment;
        private final DefaultOntopModelBuilderFragment<B> modelBuilderFragment;

        protected AbstractOntopOptimizationBuilderMixin() {
            optimizationBuilderFragment = new DefaultOntopOptimizationBuilderFragment<>((B)this);
            modelBuilderFragment= new DefaultOntopModelBuilderFragment<>((B) this);
        }

        protected Properties generateProperties() {
            // Properties from OntopModelBuilderFragmentImpl
            Properties properties = modelBuilderFragment.generateProperties();
            // Higher priority (however should be orthogonal) for the OntopOptimizationBuilderFragment.
            properties.putAll(optimizationBuilderFragment.generateProperties());

            return properties;
        }

        protected OntopOptimizationOptions generateOntopOptimizationConfigurationOptions() {
            OntopModelConfigurationOptions modelOptions = modelBuilderFragment.generateModelOptions();
            return optimizationBuilderFragment.generateOptimizationOptions(modelOptions);
        }

        @Override
        public B properties(@Nonnull Properties properties) {
            return modelBuilderFragment.properties(properties);
        }

        @Override
        public B propertyFile(File propertyFile) {
            return modelBuilderFragment.propertyFile(propertyFile);
        }

        @Override
        public B propertyFile(String propertyFilePath) {
            return modelBuilderFragment.propertyFile(propertyFilePath);
        }

        @Override
        public B enableTestMode() {
            return modelBuilderFragment.enableTestMode();
        }
    }


    public final static class BuilderImpl<B extends OntopOptimizationConfiguration.Builder<B>>
            extends AbstractOntopOptimizationBuilderMixin<B> {

        @Override
        public OntopOptimizationConfiguration build() {
            Properties properties = generateProperties();

            OntopOptimizationOptions options = generateOntopOptimizationConfigurationOptions();
            OntopOptimizationSettings settings = new OntopOptimizationSettingsImpl(properties);

            return new OntopOptimizationConfigurationImpl(settings, options);
        }
    }
}
