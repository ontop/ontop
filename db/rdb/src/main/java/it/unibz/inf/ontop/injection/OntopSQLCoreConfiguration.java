package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.injection.impl.OntopSQLCoreConfigurationImpl;

public interface OntopSQLCoreConfiguration extends OntopModelConfiguration {

    @Override
    OntopSQLCoreSettings getSettings();

    static Builder<? extends Builder<?>> defaultBuilder() {
        return new OntopSQLCoreConfigurationImpl.BuilderImpl<>();
    }

    interface OntopSQLCoreBuilderFragment<B extends Builder<B>> {
        B jdbcUrl(String jdbcUrl);
        B jdbcDriver(String jdbcDriver);
    }

    interface Builder<B extends Builder<B>> extends OntopSQLCoreBuilderFragment<B>, OntopModelConfiguration.Builder<B> {
        @Override
        OntopSQLCoreConfiguration build();
    }
}
