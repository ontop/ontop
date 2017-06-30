package it.unibz.inf.ontop.spec.trans;

import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;

public interface MappingDatatypeFiller {

    Mapping inferMissingDatatypes(Mapping mapping, TBoxReasoner tBox, ImmutableOntologyVocabulary
            vocabulary, DBMetadata dbMetadata, ExecutorRegistry executorRegistry);
}
