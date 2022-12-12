package it.unibz.inf.ontop.spec.mapping.bootstrap.engines;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Optional;

public interface BootstrappingEngine {
    Bootstrapper.BootstrappingResults bootstrapMappingAndOntology(String baseIRI,
                                                                  Optional<SQLPPMapping> inputPPMapping,
                                                                  Optional<OWLOntology> inputOntology,
                                                                  BootConf bootConf) throws MappingBootstrappingException;

    // To be overridden by specific implementations
    ImmutableList<SQLPPTriplesMap> bootstrapMappings(String baseIRI, ImmutableList<NamedRelationDefinition> tables, SQLPPMapping mapping, BootConf bootConf);
    OWLOntology bootstrapOntology(String baseIRI, Optional<OWLOntology> inputOntology, SQLPPMapping newPPMapping, BootConf bootConf) throws MappingBootstrappingException;

}