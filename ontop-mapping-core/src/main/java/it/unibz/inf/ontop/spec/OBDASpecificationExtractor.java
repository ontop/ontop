package it.unibz.inf.ontop.spec;


import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

public interface OBDASpecificationExtractor {

    OBDASpecification extract(@Nonnull File mappingFile, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException;

    OBDASpecification extract(@Nonnull Reader mappingReader, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException;

    OBDASpecification extract(@Nonnull Model mappingGraph, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException;

    OBDASpecification extract(@Nonnull PreProcessedMapping mapping, @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException;

}
