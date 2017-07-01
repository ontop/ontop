package it.unibz.inf.ontop.spec;


import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.pp.PreProcessedMapping;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.util.Optional;

public interface OBDASpecificationExtractor {

    OBDASpecification extract(@Nonnull File mappingFile, @Nonnull Optional<DBMetadata> dbMetadata,
                              @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile,
                              ExecutorRegistry executorRegistry)
            throws OBDASpecificationException;

    OBDASpecification extract(@Nonnull Reader mappingReader, @Nonnull Optional<DBMetadata> dbMetadata,
                              @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile,
                              ExecutorRegistry executorRegistry)
            throws OBDASpecificationException;

    OBDASpecification extract(@Nonnull Model mappingGraph, @Nonnull Optional<DBMetadata> dbMetadata,
                              @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile,
                              ExecutorRegistry executorRegistry)
            throws OBDASpecificationException;

    OBDASpecification extract(@Nonnull PreProcessedMapping ppMapping, @Nonnull Optional<DBMetadata> dbMetadata,
                              @Nonnull Optional<Ontology> ontology, @Nonnull Optional<File> constraintFile,
                              ExecutorRegistry executorRegistry)
            throws OBDASpecificationException;

}
