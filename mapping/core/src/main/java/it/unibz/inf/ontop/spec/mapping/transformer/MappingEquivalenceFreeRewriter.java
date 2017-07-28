package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;

public interface MappingEquivalenceFreeRewriter {

    Mapping rewrite(Mapping mapping, TBoxReasoner tBox, ImmutableOntologyVocabulary vocabulary, DBMetadata dbMetadata);
}
