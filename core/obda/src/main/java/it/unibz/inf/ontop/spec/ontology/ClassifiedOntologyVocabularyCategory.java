package it.unibz.inf.ontop.spec.ontology;

import java.util.Collection;

public interface ClassifiedOntologyVocabularyCategory<T, V> {

    Collection<V> all();

    boolean contains(String iri);

    V get(String iri);

    EquivalencesDAG<T> dag();
}
