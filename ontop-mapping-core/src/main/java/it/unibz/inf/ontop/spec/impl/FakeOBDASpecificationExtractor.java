package it.unibz.inf.ontop.spec.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.OBDASpecificationExtractor;
import it.unibz.inf.ontop.mapping.extraction.PreProcessedMapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.Ontology;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

public class FakeOBDASpecificationExtractor implements OBDASpecificationExtractor {

    private static final String MESSAGE = "This FakeDatasourceModelExtractor is fake and thus does not extract";

    @Inject
    private FakeOBDASpecificationExtractor(){
    }

    @Override
    public OBDASpecification extract(@Nonnull File mappingFile, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public OBDASpecification extract(@Nonnull Reader mappingReader, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public OBDASpecification extract(@Nonnull Model mappingGraph, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public OBDASpecification extract(@Nonnull PreProcessedMapping mapping, @Nonnull Optional<DBMetadata> dbMetadata,
                                     @Nonnull Optional<Ontology> ontology)
            throws InvalidMappingException, IOException, DuplicateMappingException {
        throw new UnsupportedOperationException(MESSAGE);
    }
}
