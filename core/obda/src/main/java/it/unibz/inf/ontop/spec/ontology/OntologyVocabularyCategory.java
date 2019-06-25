package it.unibz.inf.ontop.spec.ontology;


import org.apache.commons.rdf.api.IRI;

public interface OntologyVocabularyCategory<T> extends Iterable<T> {
    /**
     * check whether the entity has been declared and return the entity object
     *
     * @param iri
     * @return
     * @throws RuntimeException if the entity has not been declared
     */

    T get(IRI iri);

    /**
     * check whether the entity has been declared
     *
     * @param iri
     * @return
     */

    boolean contains(IRI iri);
}
