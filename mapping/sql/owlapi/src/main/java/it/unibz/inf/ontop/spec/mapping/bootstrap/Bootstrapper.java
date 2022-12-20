package it.unibz.inf.ontop.spec.mapping.bootstrap;

import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopMappingSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.MPBootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.DefaultDirectMappingBootstrapper;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Bootstraps the mapping according to the RDB Direct Mapping W3C Recommendation
 * https://www.w3.org/TR/rdb-direct-mapping/
 * TODO: Update this comment
 */
public interface Bootstrapper {

    BootstrappingResults bootstrap(OntopMappingSQLOWLAPIConfiguration configuration, String baseIRI, BootConf dictionary)
            throws MappingBootstrappingException, MappingException, OWLOntologyCreationException;

    static DefaultDirectMappingBootstrapper defaultBootstrapper() {
        return new DefaultDirectMappingBootstrapper();
    }

    static MPBootstrapper mpBootstrapper() { return new MPBootstrapper(); }

    interface BootstrappingResults {

        SQLPPMapping getPPMapping();

        OWLOntology getOntology();
    }

}
