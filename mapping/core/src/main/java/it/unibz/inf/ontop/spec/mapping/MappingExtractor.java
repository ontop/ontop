package it.unibz.inf.ontop.spec.mapping;


import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.ontology.Ontology;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface MappingExtractor {

    /**
     * TODO: in a near future, drop DBMetadata and use Mapping instead of this interface
     */
    interface MappingAndDBMetadata {
        MappingInTransformation getMapping();
        DBMetadata getDBMetadata();
    }

    MappingAndDBMetadata extract(@Nonnull OBDASpecInput specInput,
                                 @Nonnull Optional<DBMetadata> dbMetadata,
                                 @Nonnull Optional<Ontology> saturatedTBox,
                                 @Nonnull ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException;

    MappingAndDBMetadata extract(@Nonnull PreProcessedMapping ppMapping,
                                 @Nonnull OBDASpecInput specInput,
                                 @Nonnull Optional<DBMetadata> dbMetadata,
                                 @Nonnull Optional<Ontology> saturatedTBox,
                                 @Nonnull ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException;
}
