package it.unibz.inf.ontop.spec.mapping.bootstrap.impl;

import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopMappingSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.engines.impl.DirectMappingEngine;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


public class DefaultDirectMappingBootstrapper implements Bootstrapper {

    @Override
    public BootstrappingResults bootstrap(OntopMappingSQLOWLAPIConfiguration configuration, String baseIRI)
            throws MappingBootstrappingException, MappingException, OWLOntologyCreationException {

        return DirectMappingEngine.bootstrap(configuration, baseIRI);
    }
}
