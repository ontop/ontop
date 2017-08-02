package it.unibz.inf.ontop.spec.mapping.bootstrap;

import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopMappingSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.DefaultDirectMappingBootstrapper;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Bootstraps the mapping according to the RDB Direct Mapping W3C Recommendation
 * https://www.w3.org/TR/rdb-direct-mapping/
 *
 */
public interface DirectMappingBootstrapper {

    BootstrappingResults bootstrap(OntopMappingSQLOWLAPIConfiguration configuration, String baseIRI)
            throws MappingBootstrappingException, MappingException, OWLOntologyCreationException;

    static DirectMappingBootstrapper defaultBootstrapper() {
        return new DefaultDirectMappingBootstrapper();
    }


    interface BootstrappingResults {

        SQLPPMapping getPPMapping();

        OWLOntology getOntology();
    }

}
