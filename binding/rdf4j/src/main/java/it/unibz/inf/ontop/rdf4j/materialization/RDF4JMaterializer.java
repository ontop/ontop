package it.unibz.inf.ontop.rdf4j.materialization;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.rdf4j.materialization.impl.DefaultRDF4JMaterializer;
import it.unibz.inf.ontop.rdf4j.query.MaterializationGraphQuery;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import org.apache.commons.rdf.api.IRI;
import org.eclipse.rdf4j.repository.RepositoryException;

import javax.annotation.Nonnull;

public interface RDF4JMaterializer {

    /**
     * Materializes the saturated RDF graph
     */
    MaterializationGraphQuery materialize()
            throws RepositoryException;

    /**
     * Materializes a sub-set of the saturated RDF graph corresponding the selected vocabulary
     */
    MaterializationGraphQuery materialize(@Nonnull ImmutableSet<IRI> selectedVocabulary)
            throws RepositoryException;

    /**
     * Default implementation
     */
    static RDF4JMaterializer defaultMaterializer(OntopSystemConfiguration configuration, MaterializationParams materializationParams) throws OBDASpecificationException {
        return new DefaultRDF4JMaterializer(configuration, materializationParams);
    }

    /**
     * Default implementation with default parameters
     */
    static RDF4JMaterializer defaultMaterializer(OntopSystemConfiguration configuration) throws OBDASpecificationException {
        return new DefaultRDF4JMaterializer(configuration);
    }

    ImmutableSet<IRI> getClasses();
    ImmutableSet<IRI> getProperties();
}
