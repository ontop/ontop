package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.injection.impl.OntopSQLCoreConfigurationImpl;

public interface OntopSQLCoreConfiguration extends OntopOBDAConfiguration {

    @Override
    OntopSQLCoreSettings getSettings();

    static Builder<? extends Builder> defaultBuilder() {
        return new OntopSQLCoreConfigurationImpl.BuilderImpl<>();
    }

    interface OntopSQLBuilderFragment<B extends Builder<B>> {
        B jdbcName(String dbName);
        B jdbcUrl(String jdbcUrl);
        B jdbcUser(String username);
        B jdbcPassword(String password);
        B jdbcDriver(String jdbcDriver);
    }

    interface Builder<B extends Builder<B>> extends OntopSQLBuilderFragment<B>, OntopOBDAConfiguration.Builder<B> {
        @Override
        OntopSQLCoreConfiguration build();
    }
}
