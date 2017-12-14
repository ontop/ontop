package it.unibz.inf.ontop.spec.ontology;

import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.model.term.Function;

import java.util.stream.Stream;

public interface MappingVocabularyExtractor {

    Ontology extractVocabulary(Stream<? extends Function> targetAtoms);

    Ontology extractVocabulary(Mapping mapping);
}
