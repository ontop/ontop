package it.unibz.inf.ontop.spec;


import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.pp.PreProcessedMapping;

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
                                 @Nonnull Optional<Ontology> ontology, @Nonnull Optional<TBoxReasoner> saturatedTBox,
                                 @Nonnull ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException;

    MappingAndDBMetadata extract(@Nonnull PreProcessedMapping ppMapping, @Nonnull OBDASpecInput specInput,
                                 @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology,
                                 @Nonnull Optional<TBoxReasoner> saturatedTBox, @Nonnull ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException;


}
