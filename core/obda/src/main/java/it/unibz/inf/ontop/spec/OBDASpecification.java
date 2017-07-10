package it.unibz.inf.ontop.spec;


import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;

/**
 * TODO: find a better name
 */
public interface OBDASpecification {

    Mapping getSaturatedMapping();

    DBMetadata getDBMetadata();

    TBoxReasoner getSaturatedTBox();

    ImmutableOntologyVocabulary getVocabulary();
}
