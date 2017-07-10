package it.unibz.inf.ontop.spec;

import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;

import java.util.Optional;

/**
 * TODO: find a better name
 */
public interface MappingTransformer {

    OBDASpecification transform(Mapping mapping, DBMetadata dbMetadata, Optional<Ontology> optionalOntology)
            throws MappingException, OntologyException;
}
