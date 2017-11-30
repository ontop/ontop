package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.OntopTemporalSQLOWLAPIConfigurationImpl;

public interface OntopTemporalSQLOWLAPIConfiguration extends OntopSQLOWLAPIConfiguration, OntopTemporalMappingSQLAllConfiguration {

    static Builder<? extends Builder> defaultBuilder() {
        return new OntopTemporalSQLOWLAPIConfigurationImpl.BuilderImpl<>();
    }

    interface OntopTemporalSQLOWLAPIBuilderFragment<B extends Builder<B>> extends OntopTemporalMappingSQLAllBuilderFragment<B> {
    }

    interface Builder<B extends Builder<B>> extends OntopSQLOWLAPIConfiguration.Builder<B>,
            OntopTemporalMappingSQLAllConfiguration.Builder<B>, OntopTemporalSQLOWLAPIBuilderFragment<B> {

        @Override
        OntopTemporalSQLOWLAPIConfiguration build();

    }

}
