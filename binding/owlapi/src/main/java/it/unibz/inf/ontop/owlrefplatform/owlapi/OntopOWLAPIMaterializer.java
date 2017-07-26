package it.unibz.inf.ontop.owlrefplatform.owlapi;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.owlrefplatform.core.abox.MaterializationParams;
import it.unibz.inf.ontop.owlrefplatform.owlapi.impl.DefaultOntopOWLAPIMaterializer;
import org.semanticweb.owlapi.model.OWLException;

import javax.annotation.Nonnull;
import java.net.URI;

public interface OntopOWLAPIMaterializer {

    /**
     * Materializes the saturated RDF graph
     */
    OWLMaterializedGraphResultSet materialize(@Nonnull OntopSystemConfiguration configuration,
                                              @Nonnull MaterializationParams params)
            throws OWLException;

    /**
     * Materializes a sub-set of the saturated RDF graph corresponding the selected vocabulary
     */
    OWLMaterializedGraphResultSet materialize(@Nonnull OntopSystemConfiguration configuration,
                                              @Nonnull ImmutableSet<URI> selectedVocabulary,
                                              @Nonnull MaterializationParams params)
            throws OWLException;

    /**
     * Materializes the saturated RDF graph with the default options
     */
    default OWLMaterializedGraphResultSet materialize(@Nonnull OntopSystemConfiguration configuration)
            throws OWLException {
        return materialize(configuration, MaterializationParams.defaultBuilder().build());
    }

    /**
     * Materializes a sub-set of the saturated RDF graph corresponding the selected vocabulary
     * with the default options
     */
    default OWLMaterializedGraphResultSet materialize(@Nonnull OntopSystemConfiguration configuration,
                                                      @Nonnull ImmutableSet<URI> selectedVocabulary)
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
