package it.unibz.inf.ontop.owlapi.directmapping.impl;

import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopMappingSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.directmapping.DirectMappingBootstrapper;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


public class DefaultDirectMappingBootstrapper implements DirectMappingBootstrapper {

    @Override
    public BootstrappingResults bootstrap(OntopMappingSQLOWLAPIConfiguration configuration, String baseIRI)
            throws MappingBootstrappingException, MappingException, OWLOntologyCreationException {

        return DirectMappingEngine.bootstrap(configuration, baseIRI);
    }
}
