package it.unibz.inf.ontop.owlrefplatform.core.abox;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.ontology.Assertion;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.stream.Stream;

public interface OntopRDFMaterializer {

    /**
     * Materializes the saturated RDF graph
     */
    RDFMaterializationResults materialize(@Nonnull OntopSystemConfiguration configuration,
                                          boolean doStreamDBResults, boolean canBeIncomplete)
            throws OBDASpecificationException, OntopConnectionException;

    /**
     * Materializes a sub-set of the saturated RDF graph corresponding the selected vocabulary
     */
    RDFMaterializationResults materialize(@Nonnull OntopSystemConfiguration configuration,
                                          @Nonnull ImmutableSet<URI> selectedVocabulary,
                                          boolean doStreamDBResults, boolean canBeIncomplete)
            throws OBDASpecificationException, OntopConnectionException;

    /**
     * Default implementation
     */
    static OntopRDFMaterializer defaultMaterializer() {
        //return new DefaultOntopRDFMaterializer();
        throw new RuntimeException("TODO: re-enable it");
    }

    /**
     * Lazy materialization results
     */
    interface RDFMaterializationResults extends AutoCloseable {

        /**
         * Returns a stream that performs the materialization in a lazy-fashion.
         *
         * Can only be called ONCE.
         *
         */
        Stream<Assertion> getRDFStream() throws IllegalStateException;

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
        ImmutableList<URI> getPossiblyIncompleteRDFPropertiesAndClassesSoFar();

        /**
         * RDF predicates/classes that are considered for materialization.
         *
         * NB: It is possible that for some predicate/classes, no RDF triple is produced (empty answer)
         */
        ImmutableSet<URI> getSelectedVocabulary();
    }

}
