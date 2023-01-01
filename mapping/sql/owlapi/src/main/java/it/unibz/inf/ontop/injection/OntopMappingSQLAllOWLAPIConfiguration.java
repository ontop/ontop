package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.injection.impl.OntopMappingSQLAllOWLAPIConfigurationImpl;

public interface OntopMappingSQLAllOWLAPIConfiguration extends OntopMappingSQLOWLAPIConfiguration,
        OntopMappingSQLAllConfiguration {

    static <B extends Builder<B>> Builder<B> defaultBuilder() {
        return new OntopMappingSQLAllOWLAPIConfigurationImpl.BuilderImpl<>();
    }

    interface Builder<B extends Builder<B>> extends OntopMappingSQLOWLAPIConfiguration.Builder<B>,
            OntopMappingSQLAllConfiguration.Builder<B> {

        @Override
        OntopMappingSQLAllOWLAPIConfiguration build();
    }

}
