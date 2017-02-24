package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;

import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;


public class OntopSQLCoreConfigurationImpl extends OntopOBDAConfigurationImpl
        implements OntopSQLCoreConfiguration {

    private final OntopSQLCoreSettings settings;

    protected OntopSQLCoreConfigurationImpl(OntopSQLCoreSettings settings, OntopSQLOptions options) {
        super(settings, options.obdaOptions);
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
                Stream.of(new OntopSQLCommonModule(this)));
    }

    protected static class OntopSQLOptions {

        public final OntopOBDAOptions obdaOptions;

        private OntopSQLOptions(OntopOBDAOptions obdaOptions) {
            this.obdaOptions = obdaOptions;
        }
    }

    protected static class DefaultOntopSQLBuilderFragment<B extends OntopSQLCoreConfiguration.Builder<B>> implements
            OntopSQLBuilderFragment<B> {

        private final B builder;
        private Optional<String> jdbcName = Optional.empty();
        private Optional<String> jdbcUrl = Optional.empty();
        private Optional<String> jdbcUser = Optional.empty();
        private Optional<String> jbdcPassword = Optional.empty();
        private Optional<String> jdbcDriver = Optional.empty();

        DefaultOntopSQLBuilderFragment(B builder) {
            this.builder = builder;
        }

        @Override
        public B jdbcName(String dbName) {
            this.jdbcName = Optional.of(dbName);
            return builder;
        }

        @Override
        public B jdbcUrl(String jdbcUrl) {
            this.jdbcUrl = Optional.of(jdbcUrl);
            return builder;
        }

        @Override
        public B jdbcUser(String username) {
            this.jdbcUser = Optional.of(username);
            return builder;
        }

        @Override
        public B jdbcPassword(String password) {
            this.jbdcPassword = Optional.of(password);
            return builder;
        }

        @Override
        public B jdbcDriver(String jdbcDriver) {
            this.jdbcDriver = Optional.of(jdbcDriver);
            return builder;
        }

        Properties generateProperties() {
            Properties properties = new Properties();

            jdbcName.ifPresent(n -> properties.setProperty(OntopSQLCoreSettings.JDBC_NAME, n));
            jdbcUrl.ifPresent(s -> properties.setProperty(OntopSQLCoreSettings.JDBC_URL, s));
            jdbcUser.ifPresent(s -> properties.setProperty(OntopSQLCoreSettings.JDBC_USER, s));
            jbdcPassword.ifPresent(s -> properties.setProperty(OntopSQLCoreSettings.JDBC_PASSWORD, s));
            jdbcDriver.ifPresent(s -> properties.setProperty(OntopSQLCoreSettings.JDBC_DRIVER, s));

            return properties;
        }

        final OntopSQLOptions generateSQLOptions(OntopOBDAOptions obdaOptions) {
            return new OntopSQLOptions(obdaOptions);
        }

    }

    protected abstract static class OntopSQLBuilderMixin<B extends OntopSQLCoreConfiguration.Builder<B>>
            extends OntopOBDAConfigurationImpl.OntopOBDAConfigurationBuilderMixin<B>
            implements OntopSQLCoreConfiguration.Builder<B> {

        private final DefaultOntopSQLBuilderFragment<B> sqlBuilderFragment;

        protected OntopSQLBuilderMixin() {
            sqlBuilderFragment = new DefaultOntopSQLBuilderFragment<>((B)this);
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

    public static class BuilderImpl<B extends OntopSQLCoreConfiguration.Builder<B>> extends OntopSQLBuilderMixin<B> {

        @Override
        public OntopSQLCoreConfiguration build() {
            Properties properties = generateProperties();
            OntopSQLCoreSettings settings = new OntopSQLCoreSettingsImpl(properties);
            OntopSQLOptions options = generateSQLOptions();

            return new OntopSQLCoreConfigurationImpl(settings, options);
        }
    }

}
