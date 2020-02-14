package it.unibz.inf.ontop.spec.mapping.transformer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.OBDASpecification;

import java.util.Optional;

/**
 * TODO: find a better name
 */
public interface MappingTransformer {

    OBDASpecification transform(ImmutableList<MappingAssertion> mapping, DBParameters dbParameters, Optional<Ontology> ontology);
}
