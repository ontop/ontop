package it.unibz.inf.ontop.answering.resultset;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import org.apache.commons.rdf.api.IRI;

/**
 * Lazy materialization result set
 *
 * Does not allocate resources (e.g. connection) before hasNext() is called.
 *
 */
public interface MaterializedGraphResultSet extends GraphResultSet {

    /**
     * Number of RDF triples that have been materialized.
     *
     * Increases until the materialization completes.
     */
    long getTripleCountSoFar();

    /**
     * Returns true if a problem has occurred so far.
     *
     * May evolve until the materialization completes.
     *
     */
    default boolean hasEncounteredProblemsSoFar() {
        return !getPossiblyIncompleteRDFPropertiesAndClassesSoFar().isEmpty();
    }

    /**
     * RDF properties/classes for which, so far, a problem occurred during the materialization of their triples.
     *
     * May evolve until the materialization completes.
     *
     */
    ImmutableList<IRI> getPossiblyIncompleteRDFPropertiesAndClassesSoFar();

    /**
     * RDF predicates/classes that are considered for materialization.
     *
     * NB: It is possible that for some predicate/classes, no RDF triple is produced (empty answer)
     */
    ImmutableSet<IRI> getSelectedVocabulary();
}
