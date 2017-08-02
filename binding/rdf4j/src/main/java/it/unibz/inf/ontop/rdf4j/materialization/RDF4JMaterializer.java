package it.unibz.inf.ontop.rdf4j.materialization;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;

import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.rdf4j.materialization.impl.DefaultRDF4JMaterializer;
import it.unibz.inf.ontop.rdf4j.query.MaterializationGraphQuery;
import org.eclipse.rdf4j.repository.RepositoryException;

import javax.annotation.Nonnull;
import java.net.URI;

public interface RDF4JMaterializer {

    /**
     * Materializes the saturated RDF graph
     */
    MaterializationGraphQuery materialize(@Nonnull OntopSystemConfiguration configuration,
                                          @Nonnull MaterializationParams params)
            throws RepositoryException;

    /**
     * Materializes a sub-set of the saturated RDF graph corresponding the selected vocabulary
     */
    MaterializationGraphQuery materialize(@Nonnull OntopSystemConfiguration configuration,
                                          @Nonnull ImmutableSet<URI> selectedVocabulary,
                                          @Nonnull MaterializationParams params)
            throws RepositoryException;

    /**
     * Materializes the saturated RDF graph with the default options
     */
    default MaterializationGraphQuery materialize(@Nonnull OntopSystemConfiguration configuration)
            throws RepositoryException {
        return materialize(configuration, MaterializationParams.defaultBuilder().build());
    }

    /**
     * Materializes a sub-set of the saturated RDF graph corresponding the selected vocabulary
     * with the default options
     */
    default MaterializationGraphQuery materialize(@Nonnull OntopSystemConfiguration configuration,
                                                  @Nonnull ImmutableSet<URI> selectedVocabulary)
            throws RepositoryException {
        return materialize(configuration, selectedVocabulary, MaterializationParams.defaultBuilder().build());
    }

    /**
     * Default implementation
     */
    static RDF4JMaterializer defaultMaterializer() {
        return new DefaultRDF4JMaterializer();
    }


}
