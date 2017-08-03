package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.exception.DBMetadataExtractionException;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.OBDASpecInput;

import javax.annotation.Nonnull;
import java.util.Optional;


public class FakeMappingExtractor implements MappingExtractor {

    private static final String MESSAGE = "Using a FakeMappingExtractor! Please use a proper implementation instead";

    @Override
    public MappingAndDBMetadata extract(@Nonnull OBDASpecInput specInput, @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<Ontology> ontology, @Nonnull Optional<TBoxReasoner> saturatedTBox,
                                        @Nonnull ExecutorRegistry executorRegistry) throws MappingException, DBMetadataExtractionException {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public MappingAndDBMetadata extract(@Nonnull PreProcessedMapping ppMapping, @Nonnull OBDASpecInput specInput,
                                        @Nonnull Optional<DBMetadata> dbMetadata, @Nonnull Optional<Ontology> ontology,
                                        @Nonnull Optional<TBoxReasoner> saturatedTBox, @Nonnull ExecutorRegistry executorRegistry)
            throws MappingException, DBMetadataExtractionException {
        throw new UnsupportedOperationException(MESSAGE);
    }
}
