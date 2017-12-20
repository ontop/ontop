package it.unibz.inf.ontop.protege.core;

import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import org.semanticweb.owlapi.model.IRI;

public interface MutableOntologyVocabularyCategory extends Iterable<Predicate> {

    /**
     * check whether the entity has been declared
     *
     * @param iri
     * @return
     */
    // TODO: fix the rest of the code so that contains can take IRI
    boolean contains(String iri);

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
