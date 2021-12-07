package it.unibz.inf.ontop.spec.mapping.transformer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.ontology.RDFFact;

import java.util.Optional;

/**
 * TODO: find a better name
 */
public interface MappingTransformer {

    OBDASpecification transform(ImmutableList<MappingAssertion> mapping, DBParameters dbParameters,
                                Optional<Ontology> ontology, ImmutableSet<RDFFact> facts);
}
