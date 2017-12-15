package it.unibz.inf.ontop.spec.ontology;

import java.util.Collection;

public interface ClassifiedOntologyVocabularyCategory<T, V> {

    /**
     * entity names
     *
     * @return collection of entity names
     */

    Collection<V> all();

    /**
     * check whether the entity has been declared
     *
     * @param iri
     * @return true if it has been declared
     */

    boolean contains(String iri);

    /**
     * check whether the entity has been declared and return the its object
     *
     * @param iri
     * @return
     * @throws RuntimeException if the entity has not been declared
     */

    V get(String iri);

    /**
     * DAG of inclusions between entities
     *
     * @return DAG
     */

    EquivalencesDAG<T> dag();
}
