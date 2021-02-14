package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.OntopSQLCredentialConfigurationImpl;

/**
 * Also provides the credentials
 */
public interface OntopSQLCredentialConfiguration extends OntopSQLCoreConfiguration {

    @Override
    OntopSQLCredentialSettings getSettings();

    static Builder<? extends Builder<?>> defaultBuilder() {
        return new OntopSQLCredentialConfigurationImpl.BuilderImpl<>();
    }

    interface OntopSQLCredentialBuilderFragment<B extends Builder<B>> {
        B jdbcUser(String username);
        B jdbcPassword(String password);
    }

    interface Builder<B extends Builder<B>> extends OntopSQLCredentialBuilderFragment<B>,
            OntopSQLCoreConfiguration.Builder<B> {

        @Override
        OntopSQLCredentialConfiguration build();
    }
}
