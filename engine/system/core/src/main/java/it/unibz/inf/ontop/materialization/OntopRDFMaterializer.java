package it.unibz.inf.ontop.materialization;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.materialization.impl.DefaultOntopRDFMaterializer;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public interface OntopRDFMaterializer {

    /**
     * Materializes the saturated RDF graph
     */
    MaterializedGraphResultSet materialize()
            throws OBDASpecificationException;

    /**
     * Materializes a sub-set of the saturated RDF graph corresponding the selected vocabulary
     */
    MaterializedGraphResultSet materialize(@Nonnull ImmutableSet<IRI> selectedVocabulary)
            throws OBDASpecificationException;

    /**
     * Default implementation
     */
    static OntopRDFMaterializer defaultMaterializer(OntopSystemConfiguration configuration, MaterializationParams materializationParams) throws OBDASpecificationException {
        return new DefaultOntopRDFMaterializer(configuration, materializationParams);
    }

    /**
     * Default implementation
     */
    static OntopRDFMaterializer defaultMaterializer(OntopSystemConfiguration configuration) throws OBDASpecificationException {
        return new DefaultOntopRDFMaterializer(configuration, MaterializationParams.defaultBuilder().build());
    }

    ImmutableSet<IRI> getClasses();

    ImmutableSet<IRI> getProperties();
}
