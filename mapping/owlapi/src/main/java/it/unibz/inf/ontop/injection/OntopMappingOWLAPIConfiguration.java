package it.unibz.inf.ontop.injection;

import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;


public interface OntopMappingOWLAPIConfiguration extends OntopMappingOntologyConfiguration, OntopOntologyOWLAPIConfiguration {


    interface OntopMappingOWLAPIBuilderFragment<B extends Builder<B>> {

        B ontology(@Nonnull OWLOntology ontology);
    }

    interface Builder<B extends Builder<B>> extends OntopMappingOWLAPIBuilderFragment<B>,
            OntopMappingOntologyConfiguration.Builder<B> {

        @Override
        OntopMappingOWLAPIConfiguration build();
    }

}
