package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.injection.impl.OntopMappingSQLConfigurationImpl;

public interface OntopMappingSQLConfiguration extends OntopSQLConfiguration, OntopMappingConfiguration {

    @Override
    OntopMappingSQLSettings getSettings();

    /**
     * Default builder
     */
    static Builder<Builder<Builder<Builder<Builder<Builder<Builder>>>>>> defaultBuilder() {
        return new OntopMappingSQLConfigurationImpl.BuilderImpl<>();
    }

    /**
     * TODO: explain
     */
    interface OntopMappingSQLBuilderFragment<B extends Builder> {
    }

    interface Builder<B extends Builder> extends OntopMappingSQLBuilderFragment<B>,
            OntopSQLConfiguration.Builder<B>, OntopMappingConfiguration.Builder<B> {

        @Override
        OntopMappingSQLConfiguration build();
    }
}


