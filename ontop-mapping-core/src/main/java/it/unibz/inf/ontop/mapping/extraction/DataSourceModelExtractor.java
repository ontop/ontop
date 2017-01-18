package it.unibz.inf.ontop.mapping.extraction;


import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

public interface DataSourceModelExtractor {

    DataSourceModel extract(@Nonnull File mappingFile, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException;

    DataSourceModel extract(@Nonnull Reader mappingReader, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException;

    DataSourceModel extract(@Nonnull Model mappingGraph, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException;

    DataSourceModel extract(@Nonnull PreProcessedMapping mapping, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException;

}
