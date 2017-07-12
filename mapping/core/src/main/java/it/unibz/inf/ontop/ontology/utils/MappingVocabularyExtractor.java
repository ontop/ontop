package it.unibz.inf.ontop.ontology.utils;

import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.ontology.Ontology;

import java.util.stream.Stream;

public interface MappingVocabularyExtractor {

    ImmutableOntologyVocabulary extractVocabulary(Stream<? extends Function> targetAtoms);

    Ontology extractOntology(Mapping mapping);
}
