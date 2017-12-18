package it.unibz.inf.ontop.spec.ontology;


public interface OntologyVocabularyCategory<T> extends Iterable<T> {
    /**
     * check whether the entity has been declared and return the entity object
     *
     * @param uri
     * @return
     * @throws RuntimeException if the entity has not been declared
     */

    T get(String uri);

    /**
     * check whether the entity has been declared
     *
     * @param uri
     * @return
     */

    boolean contains(String uri);
}
