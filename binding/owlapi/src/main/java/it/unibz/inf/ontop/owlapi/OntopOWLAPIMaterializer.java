package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.owlapi.impl.DefaultOntopOWLAPIMaterializer;
import it.unibz.inf.ontop.owlapi.resultset.MaterializedGraphOWLResultSet;
import org.apache.commons.rdf.api.IRI;
import org.semanticweb.owlapi.model.OWLException;

import javax.annotation.Nonnull;

public interface OntopOWLAPIMaterializer {

    /**
     * Materializes the saturated RDF graph
     */
    MaterializedGraphOWLResultSet materialize(@Nonnull OntopSystemConfiguration configuration,
                                              @Nonnull MaterializationParams params)
            throws OWLException;

    /**
     * Materializes a sub-set of the saturated RDF graph corresponding the selected vocabulary
     */
    MaterializedGraphOWLResultSet materialize(@Nonnull OntopSystemConfiguration configuration,
                                              @Nonnull ImmutableSet<IRI> selectedVocabulary,
                                              @Nonnull MaterializationParams params)
            throws OWLException;

    /**
     * Materializes the saturated RDF graph with the default options
     */
    default MaterializedGraphOWLResultSet materialize(@Nonnull OntopSystemConfiguration configuration)
            throws OWLException {
        return materialize(configuration, MaterializationParams.defaultBuilder().build());
    }

    /**
     * Materializes a sub-set of the saturated RDF graph corresponding the selected vocabulary
     * with the default options
     */
    default MaterializedGraphOWLResultSet materialize(@Nonnull OntopSystemConfiguration configuration,
                                                      @Nonnull ImmutableSet<IRI> selectedVocabulary)
            throws OWLException {
        return materialize(configuration, selectedVocabulary, MaterializationParams.defaultBuilder().build());
    }

    /**
     * Default implementation
     */
    static OntopOWLAPIMaterializer defaultMaterializer() {
        return new DefaultOntopOWLAPIMaterializer();
    }

}
