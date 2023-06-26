package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopReformulationSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopReformulationSQLSettings;
import it.unibz.inf.ontop.injection.impl.OntopSQLCoreConfigurationImpl.DefaultOntopSQLCoreBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopSQLCoreConfigurationImpl.OntopSQLCoreOptions;

import javax.annotation.Nonnull;
import java.util.Properties;
import java.util.stream.Stream;
import java.io.File;

public class OntopReformulationSQLConfigurationImpl extends OntopReformulationConfigurationImpl
        implements OntopReformulationSQLConfiguration {

    private final OntopReformulationSQLSettings settings;
    private final OntopSQLCoreConfigurationImpl sqlConfiguration;

    OntopReformulationSQLConfigurationImpl(OntopReformulationSQLSettings settings,
                                           OntopReformulationSQLOptions options,
                                           SpecificationLoader specificationLoader) {
        super(settings, options.reformulationOptions, specificationLoader);
        this.settings = settings;
        this.sqlConfiguration = new OntopSQLCoreConfigurationImpl(settings, options.sqlOptions);
    }

    /**
     * Assumes the OBDA specification to be already assigned
     */
    OntopReformulationSQLConfigurationImpl(OntopReformulationSQLSettings settings,
                                           OntopReformulationSQLOptions options) {
        super(settings, options.reformulationOptions);
        this.settings = settings;
        this.sqlConfiguration = new OntopSQLCoreConfigurationImpl(settings, options.sqlOptions);
    }

    @Override
    public OntopReformulationSQLSettings getSettings() {
        return settings;
    }

    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                Stream.concat(
                        super.buildGuiceModules(),
                        sqlConfiguration.buildGuiceModules()),
                Stream.of(new OntopReformulationSQLModule(this),
                        new OntopReformulationPostModule(getSettings())));
    }

    static class OntopReformulationSQLOptions {

        final OntopReformulationOptions reformulationOptions;
        final OntopSQLCoreOptions sqlOptions;

        private OntopReformulationSQLOptions(OntopReformulationOptions reformulationOptions, OntopSQLCoreOptions sqlOptions) {
            this.reformulationOptions = reformulationOptions;
            this.sqlOptions = sqlOptions;
        }
    }

    static class DefaultOntopReformulationSQLBuilderFragment<B extends OntopReformulationSQLConfiguration.Builder<B>>
        implements OntopReformulationSQLBuilderFragment<B> {

        OntopReformulationSQLOptions generateSQLReformulationOptions(OntopReformulationOptions qaOptions,
                                                                     OntopSQLCoreOptions sqlOptions) {
            return new OntopReformulationSQLOptions(qaOptions, sqlOptions);
        }

        protected Properties generateProperties() {
            return new Properties();
        }
    }

    protected static abstract class OntopReformulationSQLBuilderMixin<B extends OntopReformulationSQLConfiguration.Builder<B>>
            extends OntopReformulationBuilderMixin<B>
            implements OntopReformulationSQLConfiguration.Builder<B> {

        private final DefaultOntopReformulationSQLBuilderFragment<B> localBuilderFragment;
        private final DefaultOntopSQLCoreBuilderFragment<B> sqlBuilderFragment;
        private final DefaultOntopModelBuilderFragment<B> modelBuilderFragment;

        OntopReformulationSQLBuilderMixin() {
            localBuilderFragment = new DefaultOntopReformulationSQLBuilderFragment<>();
            sqlBuilderFragment = new DefaultOntopSQLCoreBuilderFragment<>() {
                @Override
                protected B self() {
                    return OntopReformulationSQLBuilderMixin.this.self();
                }
            };
            modelBuilderFragment = new DefaultOntopModelBuilderFragment<>() {
                @Override
                protected B self() {
                    return OntopReformulationSQLBuilderMixin.this.self();
                }
            };
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(modelBuilderFragment.generateProperties());
            properties.putAll(sqlBuilderFragment.generateProperties());
            properties.putAll(localBuilderFragment.generateProperties());
            return properties;
        }

        protected final OntopReformulationSQLOptions generateSQLReformulationOptions() {
            OntopReformulationOptions reformulationOptions = generateReformulationOptions();
            OntopSQLCoreOptions sqlOptions = sqlBuilderFragment.generateSQLCoreOptions(
                    reformulationOptions.queryOptions.obdaOptions.modelOptions);

            return new OntopReformulationSQLOptions(reformulationOptions, sqlOptions);
        }

        @Override
        public B jdbcUrl(String jdbcUrl) {
            return sqlBuilderFragment.jdbcUrl(jdbcUrl);
        }

        @Override
        public B jdbcDriver(String jdbcDriver) {
            return sqlBuilderFragment.jdbcDriver(jdbcDriver);
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

    /**
     * Requires the OBDA specification to be already assigned
     */
    public static class BuilderImpl extends OntopReformulationSQLBuilderMixin<BuilderImpl> {

        @Override
        public OntopReformulationSQLConfiguration build() {
            if (!isOBDASpecificationAssigned())
                throw new InvalidOntopConfigurationException("An OBDA specification must be assigned " +
                        "to directly instantiate such a OntopReformulationSQLConfiguration");

            OntopReformulationSQLSettings settings = new OntopReformulationSQLSettingsImpl(generateProperties());
            OntopReformulationSQLOptions options = generateSQLReformulationOptions();
            return new OntopReformulationSQLConfigurationImpl(settings, options);
        }

        @Override
        protected BuilderImpl self() {
            return this;
        }
    }
}
