package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopSQLCredentialConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class OntopSQLCredentialConfigurationImpl extends OntopSQLCoreConfigurationImpl
        implements OntopSQLCredentialConfiguration {

    private final OntopSQLCredentialSettings settings;

    protected OntopSQLCredentialConfigurationImpl(OntopSQLCredentialSettings settings, OntopSQLCredentialOptions options) {
        super(settings, options.sqlCoreOptions);
        this.settings = settings;
    }

    protected OntopSQLCredentialConfigurationImpl(OntopSQLCredentialSettings settings, OntopSQLCredentialOptions sqlOptions,
                                                  Supplier<Injector> injectorSupplier) {
        super(settings, sqlOptions.sqlCoreOptions, injectorSupplier);
        this.settings = settings;
    }

    @Override
    public OntopSQLCredentialSettings getSettings() {
        return settings;
    }

    @Override
    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(new OntopSQLCredentialModule(this)));
    }

    protected static class OntopSQLCredentialOptions {

        public final OntopSQLCoreOptions sqlCoreOptions;

        private OntopSQLCredentialOptions(OntopSQLCoreOptions sqlCoreOptions) {
            this.sqlCoreOptions = sqlCoreOptions;
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected abstract static class DefaultOntopSQLCredentialBuilderFragment<B extends OntopSQLCredentialConfiguration.Builder<B>> implements
            OntopSQLCredentialConfiguration.OntopSQLCredentialBuilderFragment<B> {

        private Optional<String> jdbcUser = Optional.empty();
        private Optional<String> jbdcPassword = Optional.empty();

        protected abstract B self();

        @Override
        public B jdbcUser(String username) {
            this.jdbcUser = Optional.of(username);
            return self();
        }

        @Override
        public B jdbcPassword(String password) {
            this.jbdcPassword = Optional.of(password);
            return self();
        }

        protected Properties generateProperties() {
            Properties properties = new Properties();
            jdbcUser.ifPresent(s -> properties.setProperty(OntopSQLCredentialSettings.JDBC_USER, s));
            jbdcPassword.ifPresent(s -> properties.setProperty(OntopSQLCredentialSettings.JDBC_PASSWORD, s));

            return properties;
        }

        protected final OntopSQLCredentialOptions generateSQLCredentialOptions(OntopSQLCoreOptions sqlCoreOptions) {
            return new OntopSQLCredentialOptions(sqlCoreOptions);
        }
    }

    protected abstract static class OntopSQLCredentialBuilderMixin<B extends OntopSQLCredentialConfiguration.Builder<B>>
            extends OntopSQLCoreBuilderMixin<B>
            implements OntopSQLCredentialConfiguration.Builder<B> {

        private final DefaultOntopSQLCredentialBuilderFragment<B> sqlBuilderFragment;

        protected OntopSQLCredentialBuilderMixin() {
            sqlBuilderFragment = new DefaultOntopSQLCredentialBuilderFragment<>() {
                @Override
                protected B self() {
                    return OntopSQLCredentialBuilderMixin.this.self();
                }
            };
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
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(sqlBuilderFragment.generateProperties());
            return properties;
        }

        protected final OntopSQLCredentialOptions generateSQLCredentialOptions() {
            return sqlBuilderFragment.generateSQLCredentialOptions(generateSQLCoreOptions());
        }
    }

    public static class BuilderImpl extends OntopSQLCredentialBuilderMixin<BuilderImpl> {

        @Override
        public OntopSQLCredentialConfiguration build() {
            OntopSQLCredentialSettings settings = new OntopSQLCredentialSettingsImpl(generateProperties());
            OntopSQLCredentialOptions options = generateSQLCredentialOptions();

            return new OntopSQLCredentialConfigurationImpl(settings, options);
        }

        @Override
        protected BuilderImpl self() {
            return this;
        }
    }

}
