package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.OBDASpecification;

import java.util.Optional;

/**
 * TODO: find a better name
 */
public interface MappingTransformer {

    OBDASpecification transform(MappingWithProvenance mapping, DBMetadata dbMetadata, Optional<Ontology> ontology);
}
