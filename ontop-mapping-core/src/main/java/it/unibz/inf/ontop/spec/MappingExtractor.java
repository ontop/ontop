package it.unibz.inf.ontop.spec;


import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.util.Optional;

public interface MappingExtractor {

    /**
     * TODO: in a near future, drop DBMetadata and use Mapping instead of this interface
     */
    interface MappingAndDBMetadata {
        Mapping getMapping();
        DBMetadata getDBMetadata();
    }

    MappingAndDBMetadata extract(@Nonnull File mappingFile, @Nonnull Optional<DBMetadata> dbMetadata,
                                 @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile,
                                 ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException;

    MappingAndDBMetadata extract(@Nonnull Reader mappingReader, @Nonnull Optional<DBMetadata> dbMetadata,
                                 @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile,
                                 ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException;

    MappingAndDBMetadata extract(@Nonnull Model mappingGraph, @Nonnull Optional<DBMetadata> dbMetadata,
                                 @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile,
                                 ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException;

    MappingAndDBMetadata extract(@Nonnull PreProcessedMapping mapping, @Nonnull Optional<DBMetadata> dbMetadata,
                                 @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile,
                                 ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException;


}
