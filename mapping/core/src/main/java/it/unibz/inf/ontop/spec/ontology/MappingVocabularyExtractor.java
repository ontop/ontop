package it.unibz.inf.ontop.spec.ontology;

import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.model.term.Function;

import java.util.stream.Stream;

public interface MappingVocabularyExtractor {

    OntologyVocabulary extractVocabulary(Stream<? extends Function> targetAtoms);

    Ontology extractOntology(Mapping mapping);
}
