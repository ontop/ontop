package it.unibz.inf.ontop.rdf4j.query;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.eclipse.rdf4j.query.GraphQuery;

import java.net.URI;

/**
 * GraphQuery with additional information about the materialization
 */
public interface MaterializationGraphQuery extends GraphQuery {
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
    boolean hasEncounteredProblemsSoFar();

    /**
     * RDF properties/classes for which, so far, a problem occurred during the materialization of their triples.
     *
     * May evolve until the materialization completes.
     *
     */
    ImmutableList<URI> getPossiblyIncompleteRDFPropertiesAndClassesSoFar();

    /**
     * RDF predicates/classes that are considered for materialization.
     *
     * NB: It is possible that for some predicate/classes, no RDF triple is produced (empty answer)
     */
    ImmutableSet<URI> getSelectedVocabulary();
}
