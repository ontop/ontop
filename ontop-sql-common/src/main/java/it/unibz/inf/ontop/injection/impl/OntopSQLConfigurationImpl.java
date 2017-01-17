package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLSettings;

import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;


public class OntopSQLConfigurationImpl extends OntopOBDAConfigurationImpl
        implements OntopSQLConfiguration {

    private final OntopSQLSettings settings;

    protected OntopSQLConfigurationImpl(OntopSQLSettings settings, OntopSQLOptions options) {
        super(settings, options.obdaOptions);
        this.settings = settings;
    }

    @Override
    public OntopSQLSettings getSettings() {
        return settings;
    }

    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(new OntopSQLCommonModule(this)));
    }

    protected static class OntopSQLOptions {

        public final OntopOBDAOptions obdaOptions;

        private OntopSQLOptions(OntopOBDAOptions obdaOptions) {
            this.obdaOptions = obdaOptions;
        }
    }

    protected static class DefaultOntopSQLBuilderFragment<B extends OntopSQLConfiguration.Builder> implements
            OntopSQLBuilderFragment<B> {

        private final B builder;
        private Optional<String> dbName = Optional.empty();
        private Optional<String> jdbcUrl = Optional.empty();
        private Optional<String> dbUser = Optional.empty();
        private Optional<String> dbPassword = Optional.empty();
        private Optional<String> jdbcDriver = Optional.empty();

        DefaultOntopSQLBuilderFragment(B builder) {
            this.builder = builder;
        }

        @Override
        public B dbName(String dbName) {
            this.dbName = Optional.of(dbName);
            return builder;
        }

        @Override
        public B jdbcUrl(String jdbcUrl) {
            this.jdbcUrl = Optional.of(jdbcUrl);
            return builder;
        }

        @Override
        public B dbUser(String username) {
            this.dbUser = Optional.of(username);
            return builder;
        }

        @Override
        public B dbPassword(String password) {
            this.dbPassword = Optional.of(password);
            return builder;
        }

        @Override
        public B jdbcDriver(String jdbcDriver) {
            this.jdbcDriver = Optional.of(jdbcDriver);
            return builder;
        }

        Properties generateProperties() {
            Properties properties = new Properties();

            dbName.ifPresent(n -> properties.setProperty(OntopSQLSettings.DB_NAME, n));
            jdbcUrl.ifPresent(s -> properties.setProperty(OntopSQLSettings.JDBC_URL, s));
            dbUser.ifPresent(s -> properties.setProperty(OntopSQLSettings.DB_USER, s));
            dbPassword.ifPresent(s -> properties.setProperty(OntopSQLSettings.DB_PASSWORD, s));
            jdbcDriver.ifPresent(s -> properties.setProperty(OntopSQLSettings.JDBC_DRIVER, s));

            return properties;
        }

        final OntopSQLOptions generateSQLOptions(OntopOBDAOptions obdaOptions) {
            return new OntopSQLOptions(obdaOptions);
        }

    }

    protected abstract static class OntopSQLBuilderMixin<B extends OntopSQLConfiguration.Builder>
            extends OntopOBDAConfigurationImpl.OntopOBDAConfigurationBuilderMixin<B>
            implements OntopSQLConfiguration.Builder<B> {

        private final DefaultOntopSQLBuilderFragment<B> sqlBuilderFragment;

        protected OntopSQLBuilderMixin() {
            sqlBuilderFragment = new DefaultOntopSQLBuilderFragment<>((B)this);
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

        @Override
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(sqlBuilderFragment.generateProperties());
            return properties;
        }

        OntopSQLOptions generateSQLOptions() {
            return sqlBuilderFragment.generateSQLOptions(generateOBDAOptions());
        }
    }

    public static class BuilderImpl<B extends OntopSQLConfiguration.Builder> extends OntopSQLBuilderMixin<B> {

        @Override
        public OntopSQLConfiguration build() {
            Properties properties = generateProperties();
            OntopSQLSettings settings = new OntopSQLSettingsImpl(properties);
            OntopSQLOptions options = generateSQLOptions();

            return new OntopSQLConfigurationImpl(settings, options);
        }
    }

}
