package it.unibz.inf.ontop.spec.mapping;

import com.google.inject.ImplementedBy;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;

import javax.annotation.Nonnull;
import java.util.Optional;

//@ImplementedBy(it.unibz.inf.ontop.spec.mapping.impl.TemporalMappingExtractorImpl.class)
public interface TemporalMappingExtractor extends MappingExtractor {
    interface MappingAndDBMetadata extends MappingExtractor.MappingAndDBMetadata {
        Mapping getTemporalMapping();
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
