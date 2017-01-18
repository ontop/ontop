package it.unibz.inf.ontop.mapping.extraction.impl;

import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.mapping.extraction.DataSourceModel;
import it.unibz.inf.ontop.mapping.extraction.DataSourceModelExtractor;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

public class SQLDataSourceModelExtractor implements DataSourceModelExtractor {

    @Override
    public DataSourceModel extract(@Nonnull File mappingFile, @Nonnull Optional<DBMetadata> dbMetadata,
                                   @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new RuntimeException("TODO: support SQLMappingAndMetadataExtractor.extract(file, ...)");
    }

    @Override
    public DataSourceModel extract(@Nonnull Reader mappingReader, @Nonnull Optional<DBMetadata> dbMetadata,
                                   @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new RuntimeException("TODO: support SQLMappingAndMetadataExtractor.extract(reader, ...)");
    }

    @Override
    public DataSourceModel extract(@Nonnull Model mappingGraph, @Nonnull Optional<DBMetadata> dbMetadata,
                                   @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new RuntimeException("TODO: support SQLMappingAndMetadataExtractor.extract(graph, ...)");
    }

    @Override
    public DataSourceModel extract(@Nonnull PreProcessedMapping mapping, @Nonnull Optional<DBMetadata> dbMetadata,
                                   @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new RuntimeException("TODO: support SQLMappingAndMetadataExtractor.extract( mapping, ...)");
    }
}
