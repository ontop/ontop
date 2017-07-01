package it.unibz.inf.ontop.spec.trans;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;

public interface MappingDatatypeFiller {

    Mapping inferMissingDatatypes(Mapping mapping, TBoxReasoner tBox, ImmutableOntologyVocabulary
            vocabulary, DBMetadata dbMetadata, ExecutorRegistry executorRegistry);
}
