package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.mapping.pp.PreProcessedMapping;
import it.unibz.inf.ontop.spec.mapping.MappingExtractor;
import it.unibz.inf.ontop.spec.OBDASpecInput;
import it.unibz.inf.ontop.spec.ontology.Ontology;

import javax.annotation.Nonnull;
import java.util.Optional;


public class FakeMappingExtractor implements MappingExtractor {

    private static final String MESSAGE = "Using a FakeMappingExtractor! Please use a proper implementation instead";

    @Override
    public MappingAndDBMetadata extract(@Nonnull OBDASpecInput specInput,
                                        @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<Ontology> ontology,
                                        @Nonnull ExecutorRegistry executorRegistry) {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public MappingAndDBMetadata extract(@Nonnull PreProcessedMapping ppMapping,
                                        @Nonnull OBDASpecInput specInput,
                                        @Nonnull Optional<DBMetadata> dbMetadata,
                                        @Nonnull Optional<Ontology> ontology,
                                        @Nonnull ExecutorRegistry executorRegistry) {
        throw new UnsupportedOperationException(MESSAGE);
    }
}
