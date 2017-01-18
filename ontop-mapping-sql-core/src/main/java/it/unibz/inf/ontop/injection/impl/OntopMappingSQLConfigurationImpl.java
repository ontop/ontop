package it.unibz.inf.ontop.injection.impl;


import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopMappingSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.injection.impl.OntopMappingConfigurationImpl.OntopMappingOptions;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;

import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class OntopMappingSQLConfigurationImpl extends OntopSQLConfigurationImpl implements OntopMappingSQLConfiguration {

    private final OntopMappingSQLSettings settings;
    private final OntopMappingSQLOptions options;
    private final OntopMappingConfigurationImpl mappingConfiguration;

    OntopMappingSQLConfigurationImpl(OntopMappingSQLSettings settings,
                                        OntopMappingSQLOptions options) {
        super(settings, options.sqlOptions);
        this.settings = settings;
        this.options = options;
        this.mappingConfiguration = new OntopMappingConfigurationImpl(settings, options.mappingOptions);
    }

    /**
     * To be overloaded
     *
     */
    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(Stream.concat(
                    super.buildGuiceModules(),
                    mappingConfiguration.buildGuiceModules()),
                Stream.of(new OntopMappingSQLModule(this)));
    }

    @Override
    public Optional<ImplicitDBConstraintsReader> getImplicitDBConstraintsReader() {
        return mappingConfiguration.getImplicitDBConstraintsReader();
    }

    @Override
    public OntopMappingSQLSettings getSettings() {
        return settings;
    }


    /**
     * Groups all the options required by the OBDAConfiguration.
     *
     * Useful for extensions
     *
     */
    public static class OntopMappingSQLOptions {
        final OntopSQLOptions sqlOptions;
        final OntopMappingOptions mappingOptions;

        private OntopMappingSQLOptions(OntopSQLOptions sqlOptions, OntopMappingOptions mappingOptions) {
            this.sqlOptions = sqlOptions;
            this.mappingOptions = mappingOptions;
        }
    }

    protected static class DefaultMappingSQLBuilderFragment<B extends OntopMappingSQLConfiguration.Builder>
            implements OntopMappingSQLBuilderFragment<B> {

        private final B builder;

        /**
         * Default constructor
         */
        protected DefaultMappingSQLBuilderFragment(B builder) {
            this.builder = builder;
        }


        final OntopMappingSQLOptions generateMappingSQLOptions(OntopSQLOptions sqlOptions,
                                                               OntopMappingOptions mappingOptions) {
            return new OntopMappingSQLOptions(sqlOptions, mappingOptions);
        }

        Properties generateProperties() {
            return new Properties();
        }
    }

    protected abstract static class OntopMappingSQLBuilderMixin<B extends OntopMappingSQLConfiguration.Builder>
            extends OntopMappingConfigurationImpl.OntopMappingBuilderMixin<B>
            implements OntopMappingSQLConfiguration.Builder<B> {

        private final DefaultMappingSQLBuilderFragment<B> localBuilderFragment;
        private final DefaultOntopSQLBuilderFragment<B> sqlBuilderFragment;

        protected OntopMappingSQLBuilderMixin() {
            B builder = (B) this;
            localBuilderFragment = new DefaultMappingSQLBuilderFragment<>(builder);
            sqlBuilderFragment = new DefaultOntopSQLBuilderFragment<>(builder);
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(localBuilderFragment.generateProperties());
            properties.putAll(sqlBuilderFragment.generateProperties());
            return properties;
        }

        final OntopMappingSQLOptions generateMappingSQLOptions() {
            OntopOBDAOptions obdaOptions = generateOBDAOptions();

            return localBuilderFragment.generateMappingSQLOptions(
                    sqlBuilderFragment.generateSQLOptions(obdaOptions),
                    generateMappingOptions(obdaOptions));
        }

        @Override
        public B dbName(String dbName) {
            return sqlBuilderFragment.dbName(dbName);
        }

        @Override
        public B jdbcUrl(String jdbcUrl) {
            return sqlBuilderFragment.jdbcUrl(jdbcUrl);
        }

        @Override
        public B dbUser(String username) {
            return sqlBuilderFragment.dbUser(username);
        }

        @Override
        public B dbPassword(String password) {
            return sqlBuilderFragment.dbPassword(password);
        }

        @Override
        public B jdbcDriver(String jdbcDriver) {
            return sqlBuilderFragment.jdbcDriver(jdbcDriver);
        }
    }


    public static class BuilderImpl<B extends OntopMappingSQLConfiguration.Builder>
            extends OntopMappingSQLBuilderMixin<B> {

        @Override
        public OntopMappingSQLConfiguration build() {
            Properties properties = generateProperties();
            OntopMappingSQLSettings settings = new OntopMappingSQLSettingsImpl(properties);

            OntopMappingSQLOptions options = generateMappingSQLOptions();

            return new OntopMappingSQLConfigurationImpl(settings, options);
        }
    }

}
