package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.OntopSQLOWLAPIConfigurationImpl;

public interface OntopSQLOWLAPIConfiguration extends OntopStandaloneSQLConfiguration, OntopMappingSQLAllOWLAPIConfiguration {

    static <B extends Builder<B>> Builder<B> defaultBuilder() {
        return new OntopSQLOWLAPIConfigurationImpl.BuilderImpl<>();
    }

    interface Builder<B extends Builder<B>> extends OntopStandaloneSQLConfiguration.Builder<B>,
            OntopMappingSQLAllOWLAPIConfiguration.Builder<B> {

        @Override
        OntopSQLOWLAPIConfiguration build();
    }

}
