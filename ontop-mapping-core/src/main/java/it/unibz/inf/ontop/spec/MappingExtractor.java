package it.unibz.inf.ontop.spec;


import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.util.Optional;

public interface MappingExtractor<T extends DBMetadata>{

    /**
     * TODO: in a near future, drop DBMetadata and use Mapping instead of this interface
     */
    interface MappingAndDBMetadata<T extends DBMetadata> {
        Mapping getMapping();
        T getDBMetadata();
    }

//    MappingAndDBMetadata extract(@Nonnull File mappingFile, @Nonnull Optional<T> dbMetadata,
//                                 @Nonnull Optional<TBoxReasoner> tBox, @Nonnull Optional<File> constraintsFile,
//                                 ExecutorRegistry executorRegistry)
//            throws MappingException, DBMetadataExtractionException;
//
//    MappingAndDBMetadata extract(@Nonnull Reader mappingReader, @Nonnull Optional<T> dbMetadata,
//                                 @Nonnull Optional<TBoxReasoner> tBox, @Nonnull Optional<File> constraintsFile,
//                                 ExecutorRegistry executorRegistry)
//            throws MappingException, DBMetadataExtractionException;
//
//    MappingAndDBMetadata extract(@Nonnull Model mappingGraph, @Nonnull Optional<T> dbMetadata,
//                                 @Nonnull Optional<TBoxReasoner> tBox, @Nonnull Optional<File> constraintsFile,
//                                 ExecutorRegistry executorRegistry)
//            throws MappingException, DBMetadataExtractionException;

    MappingAndDBMetadata extract(@Nonnull PreProcessedMapping mapping, @Nonnull Optional<T> dbMetadata,
                                 @Nonnull Optional<TBoxReasoner> tBox, @Nonnull Optional<File> constraintsFile,
                                 ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException;

    PreProcessedMapping loadPPMapping(File mappingFile);

    PreProcessedMapping loadPPMapping(Reader mappingReader);

    PreProcessedMapping loadPPMapping(Model mappingGraph);

}
