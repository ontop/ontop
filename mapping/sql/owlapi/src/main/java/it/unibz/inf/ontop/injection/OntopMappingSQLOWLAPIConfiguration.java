package it.unibz.inf.ontop.injection;


public interface OntopMappingSQLOWLAPIConfiguration extends OntopMappingSQLConfiguration, OntopMappingOWLAPIConfiguration {

    interface Builder<B extends Builder<B>> extends OntopMappingSQLConfiguration.Builder<B>,
        OntopMappingOWLAPIConfiguration.Builder<B> {

        @Override
        OntopMappingSQLOWLAPIConfiguration build();
    }
}
