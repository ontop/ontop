package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Injector;
import com.google.inject.Module;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLSettings;
import it.unibz.inf.ontop.injection.OntopSystemSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;
import it.unibz.inf.ontop.injection.impl.OntopSQLCredentialConfigurationImpl.DefaultOntopSQLCredentialBuilderFragment;

import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class OntopSystemSQLConfigurationImpl extends OntopReformulationSQLConfigurationImpl
        implements OntopSystemSQLConfiguration {

    private final OntopSystemSQLSettings settings;

    OntopSystemSQLConfigurationImpl(OntopSystemSQLSettings settings, OntopSystemSQLOptions options,
                                    SpecificationLoader specificationLoader) {
        super(settings, options.sqlTranslationOptions, specificationLoader);
        this.settings = settings;
    }

    OntopSystemSQLConfigurationImpl(OntopSystemSQLSettings settings, OntopSystemSQLOptions options) {
        super(settings, options.sqlTranslationOptions);
        this.settings = settings;
    }

    OntopSystemSQLConfigurationImpl(OntopStandaloneSQLSettings settings, OntopSystemSQLOptions options, SpecificationLoader specificationLoader, Supplier<Injector> injectorSupplier) {
        super(settings, options.sqlTranslationOptions, specificationLoader, injectorSupplier);
        this.settings = settings;
    }

    OntopSystemSQLConfigurationImpl(OntopSystemSQLSettings settings, OntopSystemSQLOptions options, Supplier<Injector> injectorSupplier) {
        super(settings, options.sqlTranslationOptions, injectorSupplier);
        this.settings = settings;
    }

    @Override
    public OntopSystemSQLSettings getSettings() {
        return settings;
    }

    protected Stream<Module> buildGuiceModules() {
        return Stream.concat(
                super.buildGuiceModules(),
                Stream.of(
                        new OntopSystemModule(settings),
                        new OntopSystemSQLModule(settings),
                        new OntopSystemPostModule(settings)));
    }

    static class OntopSystemSQLOptions {
        final OntopReformulationSQLOptions sqlTranslationOptions;
        final OntopSQLCredentialConfigurationImpl.OntopSQLCredentialOptions sqlOptions;

        /**
         * TODO: make it private when there will be a OntopSystemSQLBuilderFragment
         */
        OntopSystemSQLOptions(OntopReformulationSQLOptions sqlTranslationOptions,
                              OntopSQLCredentialConfigurationImpl.OntopSQLCredentialOptions ontopSQLCredentialOptions) {
            this.sqlTranslationOptions = sqlTranslationOptions;
            this.sqlOptions = ontopSQLCredentialOptions;
        }
    }

    protected static abstract class OntopSystemSQLBuilderMixin<B extends OntopSystemSQLConfiguration.Builder<B>>
            extends OntopReformulationSQLBuilderMixin<B>
            implements OntopSystemSQLConfiguration.Builder<B> {

        private final DefaultOntopSQLCredentialBuilderFragment<B> sqlBuilderFragment;

        OntopSystemSQLBuilderMixin() {
            sqlBuilderFragment = new DefaultOntopSQLCredentialBuilderFragment<>() {
                @Override
                protected B self() {
                    return OntopSystemSQLBuilderMixin.this.self();
                }
            };
        }

        @Override
        protected Properties generateProperties() {
            Properties properties = super.generateProperties();
            properties.putAll(sqlBuilderFragment.generateProperties());
            return properties;
        }

        @Override
        public B jdbcUser(String username) {
            return sqlBuilderFragment.jdbcUser(username);
        }

        @Override
        public B jdbcPassword(String password) {
            return sqlBuilderFragment.jdbcPassword(password);
        }

        protected final OntopSystemSQLOptions generateSystemSQLOptions() {
            OntopReformulationSQLOptions reformulationOptions = generateSQLReformulationOptions();

            return new OntopSystemSQLOptions(reformulationOptions, sqlBuilderFragment.generateSQLCredentialOptions(
                    reformulationOptions.sqlOptions));
        }
    }

    /**
     * Requires the OBDA specification to be already assigned
     */
    public static class BuilderImpl extends OntopSystemSQLBuilderMixin<BuilderImpl> {

        @Override
        public OntopSystemSQLConfiguration build() {
            if (!isOBDASpecificationAssigned())
                throw new InvalidOntopConfigurationException("An OBDA specification must be assigned " +
                        "to directly instantiate such a OntopReformulationSQLConfiguration");

            OntopSystemSQLSettings settings = new OntopSystemSQLSettingsImpl(generateProperties());
            OntopSystemSQLOptions options = generateSystemSQLOptions();
            return new OntopSystemSQLConfigurationImpl(settings, options);
        }

        @Override
        protected BuilderImpl self() {
            return this;
        }
    }
}
