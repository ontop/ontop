package it.unibz.inf.ontop.spec.mapping.bootstrap.impl;

import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopMappingSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.engines.impl.MPEngine;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.BootConf;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class MPBootstrapper implements Bootstrapper {
    @Override
    public BootstrappingResults bootstrap(OntopMappingSQLOWLAPIConfiguration configuration, String baseIRI, BootConf bootConf) throws MappingBootstrappingException, MappingException, OWLOntologyCreationException {
        MPEngine engine = configuration.getInjector().getInstance(MPEngine.class);

        return engine.bootstrapMappingAndOntology(baseIRI, configuration.loadPPMapping(),
                configuration.loadInputOntology(), bootConf);
    }
}
