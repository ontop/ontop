package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.injection.impl.OntopSQLConfigurationImpl;

public interface OntopSQLConfiguration extends OntopModelConfiguration {

    @Override
    OntopSQLSettings getSettings();

    static Builder<Builder<Builder<Builder>>> defaultBuilder() {
        return new OntopSQLConfigurationImpl.BuilderImpl<>();
    }

    interface OntopSQLBuilderFragment<B extends Builder> {
        B dbName(String dbName);
        B jdbcUrl(String jdbcUrl);
        B dbUser(String username);
        B dbPassword(String password);
        B jdbcDriver(String jdbcDriver);
    }

    interface Builder<B extends Builder> extends OntopSQLBuilderFragment<B>, OntopModelConfiguration.Builder<B> {
        @Override
        OntopSQLConfiguration build();
    }
}
