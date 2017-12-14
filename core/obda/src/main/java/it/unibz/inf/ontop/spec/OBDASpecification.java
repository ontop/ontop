package it.unibz.inf.ontop.spec;


import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;

/**
 * TODO: find a better name
 */
public interface OBDASpecification {

    Mapping getSaturatedMapping();

    DBMetadata getDBMetadata();

    TBoxReasoner getSaturatedTBox();
}
