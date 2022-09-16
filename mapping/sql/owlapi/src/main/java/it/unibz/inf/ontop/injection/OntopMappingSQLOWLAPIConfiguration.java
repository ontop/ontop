package it.unibz.inf.ontop.injection;


public interface OntopMappingSQLOWLAPIConfiguration extends OntopMappingSQLConfiguration, OntopOntologyOWLAPIConfiguration,
        OntopMappingOntologyConfiguration {

    interface Builder<B extends Builder<B>> extends OntopMappingSQLConfiguration.Builder<B>,
        OntopOntologyOWLAPIConfiguration.Builder<B>, OntopMappingOntologyConfiguration.Builder<B> {

        @Override
        OntopMappingSQLOWLAPIConfiguration build();
    }
}
