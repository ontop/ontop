package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopReformulationConfiguration;
import it.unibz.inf.ontop.injection.OntopReformulationSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopReformulationSQLSettings;
import it.unibz.inf.ontop.injection.impl.OntopSQLCoreConfigurationImpl.DefaultOntopSQLBuilderFragment;
import it.unibz.inf.ontop.injection.impl.OntopSQLCoreConfigurationImpl.OntopSQLOptions;

import java.util.Properties;
import java.util.stream.Stream;

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
        final OntopSQLOptions sqlOptions;

        private OntopReformulationSQLOptions(OntopReformulationOptions reformulationOptions, OntopSQLOptions sqlOptions) {
            this.reformulationOptions = reformulationOptions;
            this.sqlOptions = sqlOptions;
        }
    }

    static class DefaultOntopReformulationSQLBuilderFragment<B extends OntopReformulationSQLConfiguration.Builder<B>>
        implements OntopReformulationSQLBuilderFragment<B> {

        private final B builder;

        DefaultOntopReformulationSQLBuilderFragment(B builder) {
            this.builder = builder;
        }

        OntopReformulationSQLOptions generateSQLReformulationOptions(OntopReformulationOptions qaOptions,
                                                                     OntopSQLOptions sqlOptions) {
            return new OntopReformulationSQLOptions(qaOptions, sqlOptions);
        }

        Properties generateProperties() {
            return new Properties();
        }
    }

    static abstract class OntopReformulationSQLBuilderMixin<B extends OntopReformulationSQLConfiguration.Builder<B>>
            extends OntopReformulationBuilderMixin<B>
            implements OntopReformulationSQLConfiguration.Builder<B> {

        private final DefaultOntopReformulationSQLBuilderFragment<B> localBuilderFragment;
        private final DefaultOntopSQLBuilderFragment<B> sqlBuilderFragment;

        OntopReformulationSQLBuilderMixin() {
            B builder = (B) this;
            localBuilderFragment = new DefaultOntopReformulationSQLBuilderFragment<>(builder);
            sqlBuilderFragment = new DefaultOntopSQLBuilderFragment<>(builder);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(sqlBuilderFragment.generateProperties());
            properties.putAll(localBuilderFragment.generateProperties());
            return properties;
        }

        OntopReformulationSQLOptions generateSQLReformulationOptions() {
            OntopReformulationOptions reformulationOptions = generateReformulationOptions();
            OntopSQLOptions sqlOptions = sqlBuilderFragment.generateSQLOptions(
                    reformulationOptions.obdaOptions.modelOptions);

            return new OntopReformulationSQLOptions(reformulationOptions, sqlOptions);
        }

        @Override
        public B jdbcName(String dbName) {
            return sqlBuilderFragment.jdbcName(dbName);
        }

        @Override
        public B jdbcUrl(String jdbcUrl) {
            return sqlBuilderFragment.jdbcUrl(jdbcUrl);
        }

        @Override
        public B jdbcUser(String username) {
            return sqlBuilderFragment.jdbcUser(username);
        }

        @Override
        public B jdbcPassword(String password) {
            return sqlBuilderFragment.jdbcPassword(password);
        }

        @Override
        public B jdbcDriver(String jdbcDriver) {
            return sqlBuilderFragment.jdbcDriver(jdbcDriver);
        }
    }

    /**
     * Requires the OBDA specification to be already assigned
     */
    public static class BuilderImpl<B extends OntopReformulationSQLConfiguration.Builder<B>>
        extends OntopReformulationSQLBuilderMixin<B> {

        @Override
        public OntopReformulationSQLConfiguration build() {
            if (!isOBDASpecificationAssigned())
                throw new InvalidOntopConfigurationException("An OBDA specification must be assigned " +
                        "to directly instantiate such a OntopReformulationSQLConfiguration");

            OntopReformulationSQLSettings settings = new OntopReformulationSQLSettingsImpl(generateProperties());
            OntopReformulationSQLOptions options = generateSQLReformulationOptions();
            return new OntopReformulationSQLConfigurationImpl(settings, options);
        }
    }

}
