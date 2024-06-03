package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopKGQueryConfiguration;
import it.unibz.inf.ontop.injection.OntopKGQuerySettings;
import it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl.DefaultOntopOptimizationBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl.OntopOptimizationOptions;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;


public abstract class OntopKGQueryConfigurationImpl extends OntopOBDAConfigurationImpl implements OntopKGQueryConfiguration {

    private final OntopKGQuerySettings settings;
    private final OntopOptimizationConfigurationImpl optimizationConfiguration;

    OntopKGQueryConfigurationImpl(OntopKGQuerySettings settings, OntopKGQueryOptions options) {
        super(settings, options.obdaOptions);
        this.settings = settings;
        this.optimizationConfiguration = new OntopOptimizationConfigurationImpl(settings, options.optimizationOptions, this::getInjector);
    }

    OntopKGQueryConfigurationImpl(OntopKGQuerySettings settings, OntopKGQueryOptions options,
                                  Supplier<Injector> injectorSupplier) {
        super(settings, options.obdaOptions, injectorSupplier);
        this.settings = settings;
        this.optimizationConfiguration = new OntopOptimizationConfigurationImpl(settings, options.optimizationOptions, injectorSupplier);
    }

    @Override
    public OntopKGQuerySettings getSettings() {
        return settings;
    }

    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                Stream.concat(
                        super.buildGuiceModules(),
                        optimizationConfiguration.buildGuiceModules()),
                Stream.of(new OntopKGQueryModule(settings)));
    }

    static class OntopKGQueryOptions {

        final OntopOBDAOptions obdaOptions;
        final OntopOptimizationOptions optimizationOptions;

        private OntopKGQueryOptions(OntopOBDAOptions obdaOptions, OntopOptimizationOptions optimizationOptions) {
            this.obdaOptions = obdaOptions;
            this.optimizationOptions = optimizationOptions;
        }
    }

    protected static abstract class OntopKGQueryBuilderMixin<B extends OntopKGQueryConfiguration.Builder<B>>
        extends OntopOBDAConfigurationBuilderMixin<B>
        implements OntopKGQueryConfiguration.Builder<B> {
        private final DefaultOntopOptimizationBuilderFragment<B> optimizationBuilderFragment;
        private final DefaultOntopModelBuilderFragment<B> modelBuilderFragment;

        OntopKGQueryBuilderMixin() {
            this.optimizationBuilderFragment = new DefaultOntopOptimizationBuilderFragment<>();
            this.modelBuilderFragment = new DefaultOntopModelBuilderFragment<>() {
                @Override
                protected B self() {
                    return OntopKGQueryBuilderMixin.this.self();
                }
            };
        }

        protected final OntopKGQueryOptions generateKGQueryOptions() {
            return generateKGQueryOptions(generateOBDAOptions());
        }

        protected final OntopKGQueryOptions generateKGQueryOptions(OntopOBDAOptions obdaOptions) {
            return generateKGQueryOptions(obdaOptions, optimizationBuilderFragment.generateOptimizationOptions(
                    obdaOptions.modelOptions));
        }

        protected final OntopKGQueryOptions generateKGQueryOptions(OntopOBDAOptions obdaOptions,
                                                         OntopOptimizationOptions optimizationOptions) {
            return new OntopKGQueryOptions(obdaOptions, optimizationOptions);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = new Properties();
            properties.putAll(super.generateProperties());
            properties.putAll(modelBuilderFragment.generateProperties());
            properties.putAll(optimizationBuilderFragment.generateProperties());

            return properties;
        }

        @Override
        public B properties(@Nonnull Properties properties) {
            return modelBuilderFragment.properties(properties);
        }

        @Override
        public B propertyFile(String propertyFilePath) {
            return modelBuilderFragment.propertyFile(propertyFilePath);
        }

        @Override
        public B propertyFile(File propertyFile) {
            return modelBuilderFragment.propertyFile(propertyFile);
        }

        @Override
        public B enableTestMode() {
            return modelBuilderFragment.enableTestMode();
        }
    }
}
