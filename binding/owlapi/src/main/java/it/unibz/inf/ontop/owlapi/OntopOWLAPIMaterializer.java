package it.unibz.inf.ontop.owlapi;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.owlapi.impl.DefaultOntopOWLAPIMaterializer;
import it.unibz.inf.ontop.owlapi.resultset.MaterializedGraphOWLResultSet;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;

import javax.annotation.Nonnull;

public interface OntopOWLAPIMaterializer {

    /**
     * Materializes the saturated RDF graph
     */
    MaterializedGraphOWLResultSet materialize()
            throws OWLException;

    /**
     * Materializes a sub-set of the saturated RDF graph corresponding the selected vocabulary
     */
    MaterializedGraphOWLResultSet materialize(@Nonnull ImmutableSet<IRI> selectedVocabulary)
            throws OWLException;

    /**
     * Default implementation
     */
    static OntopOWLAPIMaterializer defaultMaterializer(OntopSystemConfiguration configuration, MaterializationParams materializationParams) throws OBDASpecificationException {
        return new DefaultOntopOWLAPIMaterializer(configuration, materializationParams);
    }

    /**
     * Default implementation with default parameters
     */
    static OntopOWLAPIMaterializer defaultMaterializer(OntopSystemConfiguration configuration) throws OBDASpecificationException {
        return new DefaultOntopOWLAPIMaterializer(configuration);
    }

    ImmutableSet<IRI> getClasses();

    ImmutableSet<IRI> getProperties();
}
