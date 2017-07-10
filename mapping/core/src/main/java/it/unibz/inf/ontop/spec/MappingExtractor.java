package it.unibz.inf.ontop.spec;


import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.pp.PreProcessedMapping;
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

    MappingAndDBMetadata extract(@Nonnull PreProcessedMapping mapping, @Nonnull Optional<DBMetadata> dbMetadata,
                                 @Nonnull Optional<Ontology> ontology, @Nonnull Optional<TBoxReasoner> tBox,
                                 @Nonnull Optional<File> constraintsFile, ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException;

    PreProcessedMapping loadPPMapping(File mappingFile) throws DuplicateMappingException, MappingIOException, InvalidMappingException;

    PreProcessedMapping loadPPMapping(Reader mappingReader) throws DuplicateMappingException, MappingIOException, InvalidMappingException;

    PreProcessedMapping loadPPMapping(Model mappingGraph) throws DuplicateMappingException, InvalidMappingException;

}
