package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.injection.impl.OntopMappingSQLConfigurationImpl;

public interface OntopMappingSQLConfiguration extends OntopSQLConfiguration, OntopMappingConfiguration {

    @Override
    OntopMappingSQLSettings getSettings();

    /**
     * Default builder
     */
    static Builder<? extends Builder> defaultBuilder() {
        return new OntopMappingSQLConfigurationImpl.BuilderImpl<>();
    }

    /**
     * TODO: explain
     */
    interface OntopMappingSQLBuilderFragment<B extends Builder<B>> {
    }

    interface Builder<B extends Builder<B>> extends OntopMappingSQLBuilderFragment<B>,
            OntopSQLConfiguration.Builder<B>, OntopMappingConfiguration.Builder<B> {

        @Override
        OntopMappingSQLConfiguration build();
    }
}


