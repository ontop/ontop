package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.OntopSQLOWLAPIConfigurationImpl;

public interface OntopSQLOWLAPIConfiguration extends OntopStandaloneSQLConfiguration,
        OntopSystemOWLAPIConfiguration, OntopMappingOWLAPIConfiguration {

    static Builder<? extends Builder> defaultBuilder() {
        return new OntopSQLOWLAPIConfigurationImpl.BuilderImpl<>();
    }

    interface Builder<B extends Builder<B>> extends OntopStandaloneSQLConfiguration.Builder<B>,
            OntopMappingOWLAPIConfiguration.Builder<B> {

        @Override
        OntopSQLOWLAPIConfiguration build();
    }

}
