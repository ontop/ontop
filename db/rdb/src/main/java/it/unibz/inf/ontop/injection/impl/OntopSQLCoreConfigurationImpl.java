package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.io.File;


public class OntopSQLCoreConfigurationImpl extends OntopModelConfigurationImpl
        implements OntopSQLCoreConfiguration {

    private final OntopSQLCoreSettings settings;

    protected OntopSQLCoreConfigurationImpl(OntopSQLCoreSettings settings, OntopSQLCoreOptions options) {
        super(settings, options.modelOptions);
        this.settings = settings;
    }

    protected OntopSQLCoreConfigurationImpl(OntopSQLCoreSettings settings, OntopSQLCoreOptions sqlOptions,
                                         Supplier<Injector> injectorSupplier) {
        super(settings, sqlOptions.modelOptions, injectorSupplier);
        this.settings = settings;
    }

    @Override
    public OntopSQLCoreSettings getSettings() {
        return settings;
    }

    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(new OntopSQLCoreModule(this)));
    }

    protected static class OntopSQLCoreOptions {

        public final OntopModelConfigurationOptions modelOptions;

        private OntopSQLCoreOptions(OntopModelConfigurationOptions modelOptions) {
            this.modelOptions = modelOptions;
        }
    }

    protected static class DefaultOntopSQLCoreBuilderFragment<B extends OntopSQLCoreConfiguration.Builder<B>> implements
            OntopSQLCoreBuilderFragment<B> {

        private final B builder;
        private Optional<String> jdbcUrl = Optional.empty();
        private Optional<String> jdbcDriver = Optional.empty();

        DefaultOntopSQLCoreBuilderFragment(B builder) {
            this.builder = builder;
        }

        @Override
        public B jdbcUrl(String jdbcUrl) {
            this.jdbcUrl = Optional.of(jdbcUrl);
            return builder;
        }

        @Override
        public B jdbcDriver(String jdbcDriver) {
            this.jdbcDriver = Optional.of(jdbcDriver);
            return builder;
        }

        Properties generateProperties() {
            Properties properties = new Properties();

            jdbcUrl.ifPresent(s -> properties.setProperty(OntopSQLCoreSettings.JDBC_URL, s));
            jdbcDriver.ifPresent(s -> properties.setProperty(OntopSQLCoreSettings.JDBC_DRIVER, s));

            return properties;
        }

        final OntopSQLCoreOptions generateSQLCoreOptions(OntopModelConfigurationOptions modelOptions) {
            return new OntopSQLCoreOptions(modelOptions);
        }

    }

    protected abstract static class OntopSQLCoreBuilderMixin<B extends OntopSQLCoreConfiguration.Builder<B>>
            implements OntopSQLCoreConfiguration.Builder<B> {

        private final DefaultOntopSQLCoreBuilderFragment<B> sqlBuilderFragment;
        private final DefaultOntopModelBuilderFragment<B> modelBuilderFragment;

        protected OntopSQLCoreBuilderMixin() {
            sqlBuilderFragment = new DefaultOntopSQLCoreBuilderFragment<>((B)this);
            modelBuilderFragment = new DefaultOntopModelBuilderFragment<>((B) this);
        }

        @Override
        public B jdbcUrl(String jdbcUrl) {
            return sqlBuilderFragment.jdbcUrl(jdbcUrl);
        }

        @Override
        public B jdbcDriver(String jdbcDriver) {
            return sqlBuilderFragment.jdbcDriver(jdbcDriver);
        }

        protected Properties generateProperties() {
            Properties properties = modelBuilderFragment.generateProperties();
            properties.putAll(sqlBuilderFragment.generateProperties());
            return properties;
        }

        OntopSQLCoreOptions generateSQLCoreOptions() {
            return sqlBuilderFragment.generateSQLCoreOptions(modelBuilderFragment.generateModelOptions());
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

    public static class BuilderImpl<B extends OntopSQLCoreConfiguration.Builder<B>> extends OntopSQLCoreBuilderMixin<B> {

        @Override
        public OntopSQLCoreConfiguration build() {
            Properties properties = generateProperties();
            OntopSQLCoreSettings settings = new OntopSQLCoreSettingsImpl(properties);
            OntopSQLCoreOptions options = generateSQLCoreOptions();

            return new OntopSQLCoreConfigurationImpl(settings, options);
        }
    }

}
