package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.injection.impl.OntopSQLConfigurationImpl;

public interface OntopSQLConfiguration extends OntopOBDAConfiguration {

    @Override
    OntopSQLSettings getSettings();

    static Builder<? extends Builder> defaultBuilder() {
        return new OntopSQLConfigurationImpl.BuilderImpl<>();
    }

    interface OntopSQLBuilderFragment<B extends Builder<B>> {
        B dbName(String dbName);
        B jdbcUrl(String jdbcUrl);
        B dbUser(String username);
        B dbPassword(String password);
        B jdbcDriver(String jdbcDriver);
    }

    interface Builder<B extends Builder<B>> extends OntopSQLBuilderFragment<B>, OntopOBDAConfiguration.Builder<B> {
        @Override
        OntopSQLConfiguration build();
    }
}