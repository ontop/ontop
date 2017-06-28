package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.MappingExtractor;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.util.Optional;


public class FakeMappingExtractor implements MappingExtractor<DBMetadata> {

    private static final String MESSAGE = "Using a FakeMappingExtractor! Please use a proper implementation instead";

    @Override
    public MappingAndDBMetadata extract(@Nonnull File mappingFile, @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<TBoxReasoner> tBox, Optional<File> constraintFile,
                                        ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public MappingAndDBMetadata extract(@Nonnull Reader mappingReader, @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<TBoxReasoner> tBox, @Nonnull Optional<File> constraintFile,
                                        ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public MappingAndDBMetadata extract(@Nonnull Model mappingGraph, @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<TBoxReasoner> tBox, @Nonnull Optional<File> constraintFile,
                                        ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public MappingAndDBMetadata extract(@Nonnull PreProcessedMapping mapping, @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<TBoxReasoner> tBox, @Nonnull Optional<File> constraintFile,
                                        ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException {
        throw new UnsupportedOperationException(MESSAGE);
    }
}
