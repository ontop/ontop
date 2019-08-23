package it.unibz.inf.ontop.protege.core;

import org.apache.commons.rdf.api.IRI;

public interface MutableOntologyVocabularyCategory extends Iterable<IRI> {

    /**
     * check whether the entity has been declared
     *
     * @param iri
     * @return
     */
    boolean contains(IRI iri);

    /**
     * declare an entity
     *
     * @param iri
     */

    void declare(IRI iri);

    /**
     * remove the entity
     *
     * @param iri
     */

    void remove(IRI iri);
}
