package it.unibz.inf.ontop.owlrefplatform.core.abox;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.owlrefplatform.core.abox.impl.DefaultOntopRDFMaterializer;

import javax.annotation.Nonnull;
import java.net.URI;

public interface OntopRDFMaterializer {

    /**
     * Materializes the saturated RDF graph
     */
    MaterializedGraphResultSet materialize(@Nonnull OntopSystemConfiguration configuration,
                                           @Nonnull MaterializationParams params)
            throws OBDASpecificationException, OntopConnectionException;

    /**
     * Materializes a sub-set of the saturated RDF graph corresponding the selected vocabulary
     */
    MaterializedGraphResultSet materialize(@Nonnull OntopSystemConfiguration configuration,
                                           @Nonnull ImmutableSet<URI> selectedVocabulary,
                                           @Nonnull MaterializationParams params)
            throws OBDASpecificationException, OntopConnectionException;

    /**
     * Default implementation
     */
    static OntopRDFMaterializer defaultMaterializer() {
        return new DefaultOntopRDFMaterializer();
    }

}
