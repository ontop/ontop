package it.unibz.inf.ontop.spec;


import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface MappingExtractor {

    /**
     * TODO: in a near future, drop DBMetadata and use Mapping instead of this interface
     */
    interface MappingAndDBMetadata {
        Mapping getMapping();
        DBMetadata getDBMetadata();
    }

    MappingAndDBMetadata extract(@Nonnull OBDASpecInput specInput, @Nonnull Optional<DBMetadata> dbMetadata,
                                 @Nonnull Optional<Ontology> ontology, ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException;

    MappingAndDBMetadata extract(@Nonnull PreProcessedMapping mapping, @Nonnull Optional<DBMetadata> dbMetadata,
                                 @Nonnull Optional<Ontology> ontology, @Nonnull OBDASpecInput constraintFile,
                                 ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException;


}
